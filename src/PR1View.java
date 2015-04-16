import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.jogamp.common.nio.Buffers;


/**
 * Project 1
 * Scatter plot (OpenGL) view.
 * 
 * @author Shayan Motevalli
 * @version 1
 */
public class PR1View  extends JComponent implements ActionListener, ChangeListener, GLEventListener, MouseListener, MouseMotionListener, TableModelListener {
	private static final long serialVersionUID = 1L;
	private static final int RUBBERBAND_WIDTH = 2;
	private static final int INITIAL_POINT_SIZE = 5;
	private JComboBox<String> comboBox =  null;
	private JComboBox<String> xCol = null;
	private JComboBox<String> yCol = null;
	private JButton backgroundButton = null;
	private JButton foregroundButton = null;
	private JButton selectionButton = null;
	private Color backgroundColor = Color.WHITE;
	private Color foregroundColor = Color.BLUE;
	private Color selectionColor = Color.RED;
	private boolean rubberband = false;
	private float[] rubberbandData = null;
	private int currentColor = -1;
	private int pointSize = -1;
	private int oldX = -1;
	private int oldY = -1;
	private int fragmentShader = 0;
	private int vertexShader = 0;
	private int shaderProgram = 0;
	private PR1Model model = null;
	/*
	 * The graphic view.
	 */
	private GLJPanel graph = null;

	/*
	 * Used for Vertex Buffer Object (VBO).
	 */
	private IntBuffer intBuffer = null;

	/*
	 * Used for Vertex Array Object (VAO).
	 */
	private FloatBuffer floatBuffer = null;
	private int location;

	private static final String VERTEX_SHADER =
			"#version 150\n" +
					"in vec4 vPosition;\n" +
					"uniform int pointSize;\n" +
					"uniform float scaleX;\n" +
					"uniform float scaleY;\n" +
					"uniform float translationX;\n" +
					"uniform float translationY;\n" +
					"uniform vec4 color;\n" +
					"out vec4 vColor;\n" +
					"\n" +
					"void main(void) {\n" +
					"  gl_Position = vec4(vPosition.x * scaleX + translationX, vPosition.y * scaleY + translationY, 0.0, 1.0);\n" +
					"  gl_PointSize = pointSize;\n" +
					"  vColor = color;\n" +
					"}\n";
	private static final String FRAGMENT_SHADER =
			"#version 150\n" +
					"in vec4 vColor;\n" +
					"out vec4 fColor;\n" +
					"\n" +
					"void main(void) {\n" +
					"  fColor = vColor;\n" +
					"}\n";

	/**
	 * Creates an instance of <code>HW3View</code> class.
	 * 
	 * @param m The data model for this instance.
	 */
	public PR1View(PR1Model m) {
		rubberbandData = new float[8];
		model = m;
		model.addTableModelListener(this);
		model.addChangeListener(this);
		JToolBar toolBar = new JToolBar("HW2 Tools");
		String[] sizes = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
		comboBox = new JComboBox<String>(sizes);
		comboBox.setPreferredSize(new Dimension(200,30));
		comboBox.setMaximumSize(new Dimension(200,30));
		comboBox.addActionListener(this);
		comboBox.setActionCommand("CB");
		comboBox.setSelectedIndex(INITIAL_POINT_SIZE - 1);
		toolBar.add(comboBox);
		toolBar.addSeparator();
		toolBar.add(backgroundButton = new JButton("Background"));
		backgroundButton.addActionListener(this);
		backgroundButton.setActionCommand("BB");
		backgroundButton.setBackground(backgroundColor);
		toolBar.add(foregroundButton = new JButton("Foreground"));
		foregroundButton.addActionListener(this);
		foregroundButton.setActionCommand("FB");
		foregroundButton.setBackground(foregroundColor);
		toolBar.add(selectionButton = new JButton("Selection"));
		selectionButton.setBackground(selectionColor);
		selectionButton.addActionListener(this);
		selectionButton.setActionCommand("SB");
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.PAGE_START);
		graph = new GLJPanel(new GLCapabilities(GLProfile.getMaxProgrammableCore(false)));
		graph.addGLEventListener(this);
		graph.addMouseListener(this);
		graph.addMouseMotionListener(this);
		add(graph, BorderLayout.CENTER);


		xCol = new JComboBox<String>();
		xCol.addActionListener(this);
		xCol.setActionCommand("X");
		yCol = new JComboBox<String>();
		yCol.addActionListener(this);
		yCol.setActionCommand("Y");
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.add(xCol);
		panel.add(yCol);
		add(panel, BorderLayout.PAGE_END);
	}

	/**
	 * Creates shaders and program.
	 * 
	 * @param drawable The OpenGL drawable.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		vertexShader = compile(gl, GL3.GL_VERTEX_SHADER, VERTEX_SHADER);
		fragmentShader = compile(gl, GL3.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertexShader);
		gl.glAttachShader(shaderProgram, fragmentShader);
		gl.glLinkProgram(shaderProgram);
	}

	/**
	 * Disposes of the created shaders and program.
	 * 
	 * @param drawable The OpenGL drawable.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glDetachShader(shaderProgram, vertexShader);
		gl.glDeleteShader(vertexShader);
		gl.glDetachShader(shaderProgram, fragmentShader);
		gl.glDeleteShader(fragmentShader);
		gl.glDeleteProgram(shaderProgram);
	}

	/**
	 * Displays the updated/selected data.
	 * 
	 * @param drawable The OpenGL drawable.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		float[] data = null;
		GL3 gl = drawable.getGL().getGL3();

		// Use the current values of the background color components as color vertex shader variables values.
		gl.glClearColor(backgroundColor.getRed() / 255.0f, backgroundColor.getGreen() / 255.0f, backgroundColor.getBlue() / 255.0f, 0.0f);
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

		// Create 3 VBOs (all data, selected data, rubberband data).
		intBuffer = Buffers.newDirectIntBuffer(3);
		gl.glGenBuffers(3, intBuffer);

		// Enable using gl_PointSize in the vertex shader.
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);

		// Sets the line size.
		gl.glEnable(GL3.GL_LINE_SMOOTH);			
		gl.glLineWidth(3.0f);

		// Use the shader		
		gl.glUseProgram(shaderProgram);

		// Skip if no data available
		if ((data = model.getAllData((String)xCol.getSelectedItem(), (String)yCol.getSelectedItem())) != null)  {

			// Set the first VBO (all data).
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, intBuffer.get(0));

			// Set the first VAO (all data).
			floatBuffer = Buffers.newDirectFloatBuffer(data);
			gl.glBufferData(GL3.GL_ARRAY_BUFFER, data.length * Buffers.SIZEOF_FLOAT, floatBuffer, GL3.GL_STATIC_DRAW);

			// Use all vertex data as vPosition vertex shader variable.
			location = gl.glGetAttribLocation(shaderProgram, "vPosition");
			gl.glVertexAttribPointer(location, 2, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(location);

			// Use the current value of pointSize variable as pointSize vertex shader variable.
			location = gl.glGetUniformLocation(shaderProgram, "pointSize");
			gl.glUniform1i(location, pointSize);

			// Use the current value stored in the models, getScaleX(), as scaleX vertex shader variable value.
			location = gl.glGetUniformLocation(shaderProgram, "scaleX");
			gl.glUniform1f(location, model.getScaleX((String)xCol.getSelectedItem()));

			// Use the current value stored in the model, getScaleY(), as scaleY vertex shader variable value.
			location = gl.glGetUniformLocation(shaderProgram, "scaleY");
			gl.glUniform1f(location, model.getScaleY((String)yCol.getSelectedItem()));

			// Use the current value stored in the model, getTranslationX(), as translationX vertex shader variable value.
			location = gl.glGetUniformLocation(shaderProgram, "translationX");
			gl.glUniform1f(location, model.getTranslationX((String)xCol.getSelectedItem()));

			// Use the current value stored in the model, getTranslationY(), as translationY vertex shader variable value.
			location = gl.glGetUniformLocation(shaderProgram, "translationY");
			gl.glUniform1f(location, model.getTranslationY((String)yCol.getSelectedItem()));

			// Use the current values of the foreground color components as color vertex shader variables values.
			location = gl.glGetUniformLocation(shaderProgram, "color");
			gl.glUniform4f(location, foregroundColor.getRed() / 255.0f, foregroundColor.getGreen() / 255.0f, foregroundColor.getBlue() / 255.0f, 0.0f);

			// Draw points (all data).
			gl.glDrawArrays(GL3.GL_POINTS, 0, data.length / 2);
		}

		// Skip if no selected data available
		if ((data = model.getSelectedData((String)xCol.getSelectedItem(), (String)yCol.getSelectedItem())) != null) {

			// Set the second VBO (selected data).
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, intBuffer.get(1));

			// Set the second VAO (selected data).
			floatBuffer = Buffers.newDirectFloatBuffer(data);
			gl.glBufferData(GL3.GL_ARRAY_BUFFER, data.length * Buffers.SIZEOF_FLOAT, floatBuffer, GL3.GL_STATIC_DRAW);

			// Use selected data vPosition vertex shader variable.
			location = gl.glGetAttribLocation(shaderProgram, "vPosition");
			gl.glVertexAttribPointer(location, 2, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(location);

			// Use the current values of the selection color components as color vertex shader variables values.
			location = gl.glGetUniformLocation(shaderProgram, "color");
			gl.glUniform4f(location, selectionColor.getRed() / 255.0f, selectionColor.getGreen() / 255.0f, selectionColor.getBlue() / 255.0f, 0.0f);

			// Draw points (selected data).
			gl.glDrawArrays(GL3.GL_POINTS, 0, data.length / 2);
		}

		// Skip if no rubberband data available
		if (rubberband) {
			// Set the third VBO (rubberband data).
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, intBuffer.get(2));

			// Set the third VAO (rubberband data).
			floatBuffer = Buffers.newDirectFloatBuffer(rubberbandData);
			gl.glBufferData(GL3.GL_ARRAY_BUFFER, rubberbandData.length * Buffers.SIZEOF_FLOAT, floatBuffer, GL3.GL_STATIC_DRAW);

			// Use rubberband data as vPosition vertex shader variable.
			location = gl.glGetAttribLocation(shaderProgram, "vPosition");
			gl.glVertexAttribPointer(location, 2, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(location);

			// Set the width of the rubberband rectangle as pointSize vertex shader variable.
			location = gl.glGetUniformLocation(shaderProgram, "pointSize");
			gl.glUniform1i(location, RUBBERBAND_WIDTH);

			// Draw a line loop (rubberband rectangle data).
			gl.glDrawArrays(GL3.GL_LINE_LOOP, 0, rubberbandData.length / 2);
		}

	}

	/**
	 * Overridden as an empty method.
	 *
	 * @param x The x position.
	 * @param y The y position.
	 * @param width The width.
	 * @param height The height.
	 * @param drawable OpenGL drawable.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {	}

	/**
	 * A utility method to create a shader
	 * 
	 * @param gl The OpenGL context.
	 * @param shaderType The type of the shader.
	 * @param program The string containing the program.
	 * @return the created shader.
	 */
	private int compile(GL3 gl, int shaderType, String program) {
		int shader = gl.glCreateShader(shaderType);
		String[] lines = new String[] { program };
		int[] lengths = new int[] { lines[0].length() };
		gl.glShaderSource(shader, lines.length, lines, lengths, 0);
		gl.glCompileShader(shader);
		int[] compiled = new int[1];
		gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compiled, 0);
		if(compiled[0]==0) {
			int[] logLength = new int[1];
			gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shader, logLength[0], (int[])null, 0, log, 0);
			System.err.println("Error compiling the shader: " + new String(log));
			System.exit(1);
		}
		return shader;
	}

	/**
	 * The action event handler that determines the source of the event
	 * and processes the event accordingly.
	 * 
	 * @param e The generated event.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		ColorDialog c = null;
		String command = null;
		switch (source.getClass().getName()) {
		case "javax.swing.JComboBox":
			command = ((JComboBox<String>) source).getActionCommand();
			break;
		case "javax.swing.JButton":
			command = ((JButton) source).getActionCommand();
			break;
		default:
			command = "";
		}
		switch (command) {

		case "CB":
			pointSize = comboBox.getSelectedIndex() + 1;
			repaint();
			break;
		case "BB":
			currentColor = 0;
			c =  new ColorDialog(null, backgroundColor, this);
			c.setVisible(true);			
			break;
		case "FB":
			currentColor = 1;
			c =  new ColorDialog(null, foregroundColor, this);
			c.setVisible(true);			
			break;
		case "SB":
			currentColor = 2;
			c =  new ColorDialog(null, selectionColor, this);
			c.setVisible(true);		
			break;
		case "X":
			repaint();
			break;
		case "Y":
			repaint();
		}
	}

	/**
	 * Processes a state changed event by setting the new color values (if applicable) and repaints.
	 *
	 * @param e The state changed event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof ColorSelectionModel) {
			Color c = ((ColorSelectionModel) source).getSelectedColor();
			switch (currentColor) {
			case 0:
				backgroundColor = c;
				backgroundButton.setBackground(c);
				break;
			case 1:
				foregroundColor = c;
				foregroundButton.setBackground(c);
				break;
			case 2:
				selectionColor = c;
				selectionButton.setBackground(c);
				break;
			}
		}
		repaint();
	}

	/**
	 * Repaints when a <code>TableModelEvent</code> event is received.
	 *
	 * @param e The table model event.
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getColumn() == TableModelEvent.ALL_COLUMNS) { //only changes combobox if all columns change
			xCol.removeAllItems();
			yCol.removeAllItems();
			for (int i=0; i < model.getColumnCount(); i++) {
				xCol.addItem(model.getColumnName(i));
				yCol.addItem(model.getColumnName(i));
			}
			if (model.getColumnCount() > 1) {
				yCol.setSelectedIndex(1);
			}
		}
		repaint();
	}

	/**
	 * Updates/repaints the rubberband rectangle (without updating the selction) when mouse is dragged.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (rubberband) {
			setRubberBandData(oldX, oldY, e.getX(), e.getY(), false);
			repaint();
		}
	}

	/**
	 * Overridden as an empty method.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseMoved(MouseEvent e) { }

	/**
	 * Overridden as an empty method.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseClicked(MouseEvent e) { }

	/**
	 * Initializes the rubberbanding if the left mouse button is pressed.
	 * CLears the selection if the right mouse button is pressed.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			rubberband = true;
			oldX = e.getX();
			oldY = e.getY();
			break;
		case MouseEvent.BUTTON3:
			model.setSelectedRows(null);
			break;
		}
	}

	/**
	 * Updates/repaints the rubberband rectangle and updates the selection when mouse is released.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (rubberband) {
			setRubberBandData(oldX, oldY, e.getX(), e.getY(), true);
			rubberband = false;
		}
		repaint();
	}

	/**
	 * Overridden as an empty method.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseEntered(MouseEvent e) { }

	/**
	 * Overridden as an empty method.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseExited(MouseEvent e) {	}

	/**
	 * @param x1 The x coordinate of the starting cursor location.
	 * @param y1 The y coordinate of the starting cursor location.
	 * @param x2 The x coordinate of the current cursor location.
	 * @param y2 The y coordinate of the current cursor location.
	 * @param update If true, update the slection based on the rubberband rectangle.
	 */
	private void setRubberBandData(int x1, int y1, int x2, int y2, boolean update) {
		float xmin, xmax, ymin, ymax;
		xmin = x1 < x2 ? x1 : x2;
		xmax = x1 < x2 ? x2 : x1;
		ymin = y1 > y2 ? y1 : y2;
		ymax = y1 > y2 ? y2 : y1;

		xmin = -1.0f + 2.0f * xmin / graph.getWidth();
		xmax = -1.0f + 2.0f * xmax / graph.getWidth();
		ymin = 1.0f - 2.0f * ymin / graph.getHeight();
		ymax = 1.0f - 2.0f * ymax / graph.getHeight();

		xmin = (xmin - model.getTranslationX((String)xCol.getSelectedItem())) / model.getScaleX((String)xCol.getSelectedItem());
		xmax = (xmax - model.getTranslationX((String)xCol.getSelectedItem())) / model.getScaleX((String)xCol.getSelectedItem());
		ymin = (ymin - model.getTranslationY((String)yCol.getSelectedItem())) / model.getScaleY((String)yCol.getSelectedItem());
		ymax = (ymax - model.getTranslationY((String)yCol.getSelectedItem())) / model.getScaleY((String)yCol.getSelectedItem());

		rubberbandData[0] = xmin;
		rubberbandData[1] = ymin;
		rubberbandData[2] = xmax;
		rubberbandData[3] = ymin;
		rubberbandData[4] = xmax;
		rubberbandData[5] = ymax;
		rubberbandData[6] = xmin;
		rubberbandData[7] = ymax;

		if (update) {
			model.addSelectedRows(xmin, xmax, ymin, ymax);
		}
	}
}