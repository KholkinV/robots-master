package gui;

import log.Logger;
import logic.Robot;
import model.Model;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.jws.WebParam;
import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();
    
    private static Timer initTimer() 
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }
    
    public GameVisualizer()
    {
        Model.init();

        m_timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    onRedrawEvent();
                }
            }, 0, 10);
        m_timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Model.getActiveRobot().onModelUpdateEvent();
                }
            }, 0, 10);

        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                Model.setTargetPosition(e.getPoint());
                Logger.debug("Изменена позиция цели");
                repaint();
            }

            Point firstPoint;
            Point secondPoint;
            public void mousePressed(MouseEvent e){
                firstPoint = e.getPoint();
            }

            public void mouseReleased(MouseEvent e){
                secondPoint = e.getPoint();
                int x = firstPoint.x <= secondPoint.x ? firstPoint.x : secondPoint.x;
                int y = firstPoint.y <= secondPoint.y ? firstPoint.y : secondPoint.y;
                Rectangle rect = new Rectangle(x, y,
                        Math.abs(secondPoint.x - firstPoint.x), Math.abs(secondPoint.y - firstPoint.y));
                Model.addRect(rect);
                Model.getRobots().forEach(r -> r.getNewPath());
                Logger.debug("Нарисован прямоугольник");
                repaint();
            }
        });
        setDoubleBuffered(true);
    }


    
    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        Model.getRobots().forEach(e -> {
            drawRobot(g2d, round(e.getM_robotPositionX()), round(e.getM_robotPositionY()), e.getM_robotDirection(), e);
        });
        drawTarget(g2d, Model.getM_targetPositionX(), Model.getM_targetPositionY());
        Model.getRect().forEach(e -> ((Graphics2D) g).draw(e));
    }
    
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private void drawRobot(Graphics2D g, int x, int y, double direction, Robot robot)
    {
        int robotCenterX = round(robot.getM_robotPositionX());
        int robotCenterY = round(robot.getM_robotPositionY());
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY); 
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
    }
    
    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0); 
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }
}
