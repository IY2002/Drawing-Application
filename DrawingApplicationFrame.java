/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package java2ddrawingapplication;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author Ismael Youssef
 */
public class DrawingApplicationFrame extends JFrame implements ActionListener
{

    // universal integer to keep track of shapes drawn and deleted
    int num_shapes = 0;
    
    //color used for top panels backgrounds
    Color bg = new Color(100,197,235);
    
    //Panels used for layout
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel top = new JPanel();
    JPanel draw = new DrawPanel();
    JPanel statusHolder = new JPanel();
    
    //will display mouse location in bottom left corner
    JLabel status = new JLabel(" (X, Y)");

    // goes infront of panel elements to display their text
    JLabel panel1_text = new JLabel("Shape: ");
    JLabel panel2_text = new JLabel("Options: ");

    //Various Panel componenets
    JSpinner width = new JSpinner();
    JSpinner dashLength = new JSpinner();
    JLabel width_text = new JLabel("Line Width: ");
    JLabel dash_text = new JLabel("Dash Length: ");
    JCheckBox filled = new JCheckBox();
    JCheckBox gradient = new JCheckBox();
    JCheckBox dashed = new JCheckBox();

    // Buttons
    JButton color1 = new JButton("1st Color...");
    JButton color2 = new JButton("2nd Color...");
    JButton undo = new JButton("Undo");
    JButton clear = new JButton("Clear");

    //The various shape options and how they are arranged
    String[] shapes = {"Rectangle", "Oval", "Line"};
    JComboBox<String> shape = new JComboBox<>(shapes);

    //List that holds all the shapes in the order they were made.
    //makes drawing, Undoing and clearing a lot easier
    List<MyShapes> list = new ArrayList<>();
    
    //default color choice is black
    Color first_color = Color.BLACK;
    Color second_color = Color.BLACK;
    
    //will be set later to their respective values based on user choice
    Stroke stroke;
    Paint paint;
  
    // Constructor for DrawingApplicationFrame
    public DrawingApplicationFrame()
    {
        // add panel1's components to it
        panel1.add(panel1_text);
        panel1.add(shape);
        panel1.add(color1);
        panel1.add(color2);
        panel1.add(undo);
        panel1.add(clear);
        panel1.setBackground(bg);

        // add panel2's components to it
        panel2.add(panel2_text);
        panel2.add(filled);
        panel2.add(gradient);
        panel2.add(dashed);
        panel2.add(width_text);
        panel2.add(width);
        panel2.add(dash_text);
        panel2.add(dashLength);
        panel2.setBackground(bg);

        // to know if each of these buttons were pressed
        color1.addActionListener(this);
        color2.addActionListener(this);
        undo.addActionListener(this);
        clear.addActionListener(this);

        // set text for each checkbox and make their focusbale false so it looks better
        filled.setText("Filled");
        filled.setFocusable(false);
        gradient.setText("Use Gradient");
        gradient.setFocusable(false);
        dashed.setText("Dashed");
        dashed.setFocusable(false);

        // default width and dash values
        width.setValue(5);
        dashLength.setValue(5);

        // looks nicer with it set to false
        shape.setFocusable(false);

        // Adds panel1 and panel2 to top using a 2 row 1 col grid layout
        top.setLayout(new GridLayout(2,1));
        top.add(panel1);
        top.add(panel2);
        top.setBackground(Color.CYAN);

        // Use holder to have a light gray background on status bar
        statusHolder.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusHolder.add(status);
        statusHolder.setBackground(new Color(205,205,205));
        
        // add topPanel to North, drawPanel to Center, and statusLabel to South
        this.add(top, BorderLayout.NORTH);
        this.add(draw, BorderLayout.CENTER);
        this.add(statusHolder, BorderLayout.SOUTH);
    }

    // Sets status bar to current mouse location
    public void statusUpdater(int x, int y){
        status.setText(" (" + x + ", " + y + ')');
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //check were the action came from
        if(e.getSource() == color1){
            //open color chooser and set selected color into first_color
            first_color = JColorChooser.showDialog(null, "pick a color", Color.BLACK);
        }
        if(e.getSource() == color2){
            //open color chooser and set selected color into second_color
            second_color = JColorChooser.showDialog(null, "pick a color", Color.BLACK);
        }
        if(e.getSource() == undo){
            // remove most recent shape in list
            if(num_shapes != 0){
                list.remove(num_shapes - 1);
                num_shapes--;
                draw.repaint();
            }
        }
        if(e.getSource() == clear){
            //delete all the shapes in the list 
            list.clear();
            num_shapes = 0;
            draw.repaint();
        }
    }
    
    private class DrawPanel extends JPanel
    {
        public DrawPanel()
        {
            //add mouse handlers for both actions and motions
            MouseHandler handler = new MouseHandler();
            this.addMouseListener(handler);
            this.addMouseMotionListener(handler);
            this.repaint();
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            //loop through the shapes and draw them
            int counter = 0;
            while(counter < num_shapes){
                list.get(counter).draw(g2d);
                counter++;
            }
        }

        private class MouseHandler extends MouseAdapter implements MouseMotionListener
        {
            //will be added to list every time user presses mouse in drawPanel
            MyShapes temp_shape;
            
            public void mousePressed(MouseEvent event) {
                //determine what options user picked
                //Create new shape based on those and add to list
                boolean isFilled = filled.isSelected();
                boolean isGradient = gradient.isSelected();
                boolean isDashed = dashed.isSelected();
                int line_width = (int) width.getValue();
                int dash_length = (int) dashLength.getValue();
                float[] dash = {(float) dash_length};
                
                //0:rectangle 1:Oval 2:Line
                int shape_selected = shape.getSelectedIndex();
                
                //setup the gradient if selected
                if (isGradient) paint = new GradientPaint(0,0, first_color, 50, 50, second_color, true);
                else paint = first_color;
                
                //setup dashed if selected
                if (isDashed) stroke = new BasicStroke(line_width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dash, 0);
                else stroke = new BasicStroke(line_width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

                // get current mouse location, use as both starting and ending point of shape for now
                Point start = new Point(event.getX(), event.getY());
                
                //determine what shape to draw
                switch (shape_selected) {
                    
                    case 0: // rectangle
                        temp_shape = new MyRectangle(start, start, paint, stroke, isFilled); 
                        break;
                        
                    case 1: // oval
                        temp_shape = new MyOval(start, start, paint, stroke, isFilled);
                        break;
                        
                    case 2: // line
                        temp_shape = new MyLine(start, start, paint, stroke);
                        break;
                }
                
                // add shape to list and increment shape number
                list.add(temp_shape);
                num_shapes++;
                
                //repaint panel to update what is displayed with the shape being drawn
                DrawPanel.this.repaint();
            }

            public void mouseReleased(MouseEvent event)
            {
                //change the shape drawn's endpoint to location of mouse when it is released
                temp_shape.setEndPoint(new Point(event.getX(), event.getY()));
                
                //refresh drawing on panel
                DrawPanel.this.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent event)
            {
                //update shape's endpoint with current location
                //allows our shape to be resized while the mosue is held
                temp_shape.setEndPoint(new Point(event.getX(), event.getY()));
                
                //update status bar with current mouse location
                statusUpdater(event.getX(), event.getY());
                
                //refresh drawings on panel
                DrawPanel.this.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent event)
            {
                //update status bar with current lcoation of mouse
                statusUpdater(event.getX(), event.getY());
            }
        }
    }
}