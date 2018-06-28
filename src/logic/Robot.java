package logic;

import gui.RobotsProgram;
import log.Logger;
import model.Model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingDeque;

import static java.awt.geom.Point2D.distance;

public class Robot implements Serializable{

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;
    private ArrayDeque<Point> path = null;
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

    private void rotateRobot(Point target){
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                target.x, target.y);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
        {
            angularVelocity = maxAngularVelocity;
        }
        if (angleToTarget < m_robotDirection)
        {
            angularVelocity = -maxAngularVelocity;
        }
        while(Math.abs(angleToTarget - m_robotDirection) > 0.01){
            angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                    target.x, target.y);
            moveRobot(0, angularVelocity, 10);
        }
    }

    public void getNewPath(){
        Thread thread = new Thread(() -> path = breadthSearch(new Point((int)m_robotPositionX, (int)m_robotPositionY)));
        thread.start();
    }

    public void onModelUpdateEvent()
    {
        if(path != null && !path.isEmpty()){
            rotateRobot(path.remove());
            moveRobot(velocity, 0, 10);
        }
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

    private ArrayDeque<Point> breadthSearch(Point start){
        ArrayDeque<Point> path = new ArrayDeque<>();
        ArrayDeque<Point> queue = new ArrayDeque<>();
        HashSet<Point> visited = new HashSet<>();
        HashMap<Point, Point> map = new HashMap<>();
        queue.add(start);

        while(!queue.isEmpty()){
            Point p = queue.remove();
            if(visited.contains(p)) continue;
            visited.add(p);

            ArrayList<Point> neighbours = getNeighbours(p);
            if(neighbours.size() != 0){
                for (Point neighbour: neighbours){
                    if(neighbour.x == RobotsProgram.model.getTarget().getM_targetPositionX() &&
                            neighbour.y == RobotsProgram.model.getTarget().getM_targetPositionY()){
                        map.put(neighbour, p);
                        Point temp = neighbour;
                        path.addFirst(temp);
                        while(temp.x != start.x || temp.y != start.y) {
                            path.addFirst(map.get(temp));
                            temp = map.get(temp);
                        }
                        return path;
                    }
                    if(!visited.contains(neighbour)){
                        queue.add(neighbour);
                        map.put(neighbour, p);
                    }
                }
            }
        }
        return path;
    }

    private ArrayList<Point> getNeighbours(Point location){
        Point[] neighbours = new Point[]{
                new Point(location.x, location.y - 1),
                new Point(location.x + 1, location.y),
                new Point(location.x, location.y + 1),
                new Point(location.x - 1, location.y)
        };
        boolean flag = false;
        ArrayList<Point> result = new ArrayList<>();
        ArrayList<Rectangle> rect = RobotsProgram.model.getRect();
        for (Point p : neighbours){
            if(p.x >= 0 && p.y >= 0 && p.x < 800 && p.y < 800){
                for (Rectangle e : rect) {
                    if (e.contains(p)) flag = true;
                }
                if(!flag) {
                    result.add(p);
                    flag = false;
                }
            }
        }
        return result;
    }
}
