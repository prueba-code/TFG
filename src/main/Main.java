package main;

import org.joml.Random;
import org.joml.Vector2d;
import utils.render.Window;
import world.World;

import java.util.Arrays;

public class Main {
    //https://www.youtube.com/watch?v=88oZT7Aum6s&list=PLtrSb4XxIVbp8AKuEAlwNXDxr99e3woGE&index=3

    public static final World WORLD = new World(new Random().nextInt(100000000), 500);
    public static boolean isDebugging = false;

    public static void main(String[] args) {
        for (String argument: args) {
            if (argument.equals("--activeDebug")) {
                isDebugging = true;
                break;
            }
        }
        Window.run();
    }
}