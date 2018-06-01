package gui;

import model.Model;

import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RobotsProgram
{
    public static Model model = new Model();
    public static MainApplicationFrame frame = new MainApplicationFrame();
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("model.dat")))
        {
            model = (Model) ois.readObject();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("frame.dat"))){
            frame = (MainApplicationFrame) ois.readObject();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> {

            frame.pack();
            frame.setVisible(true);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);

        });
    }}
