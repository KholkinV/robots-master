package logic;

import log.Logger;
import model.Model;

import java.awt.*;

import static java.awt.geom.Point2D.distance;

public class Robot {

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    private volatile double velocity = Model.paused ? 0 : maxVelocity;

    public double getM_robotPositionX() {
        return m_robotPositionX;
    }
    public double getM_robotPositionY() {
        return m_robotPositionY;
    }

    public double getM_robotDirection() {
        return m_robotDirection;
    }

    public static double getMaxVelocity() {
        return maxVelocity;
    }
    public static double getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {

        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);
        double newX = m_robotPositionX + velocity / angularVelocity *
                (Math.sin(m_robotDirection  + angularVelocity * duration) -
                        Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
                (Math.cos(m_robotDirection  + angularVelocity * duration) -
                        Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }
        m_robotPositionX = newX;
        m_robotPositionY = newY;
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
        m_robotDirection = newDirection;
    }

    private void rotateRobot(){
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                Model.getM_targetPositionX(), Model.getM_targetPositionY());
        double angularVelocity = 0;
        while(Math.abs(angleToTarget - m_robotDirection) > 0.01){
            if (angleToTarget > m_robotDirection)
            {
                angularVelocity = maxAngularVelocity;
            }
            if (angleToTarget < m_robotDirection)
            {
                angularVelocity = -maxAngularVelocity;
            }

            angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                    Model.getM_targetPositionX(), Model.getM_targetPositionY());
            moveRobot(0, angularVelocity, 10);
        }
    }

    public void onModelUpdateEvent()
    {
        double distance = distance(Model.getM_targetPositionX(), Model.getM_targetPositionY(),
                m_robotPositionX, m_robotPositionY);
        if (distance < 0.5)
        {
            return;
        }
        rotateRobot();
        moveRobot(velocity, 0, 10);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }
}
