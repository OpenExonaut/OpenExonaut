package xyz.openatbp.extension.pathfinding;

import java.awt.geom.Point2D;
import java.util.*;

import com.smartfoxserver.v2.entities.Room;

import xyz.openatbp.extension.ATBPExtension;
import xyz.openatbp.extension.Console;
import xyz.openatbp.extension.ExtensionCommands;

public class Node {
    public static final double SIZE = 0.5;
    private int col;
    private int row;
    private boolean solid;
    private double x;
    private double y;

    public Node(int col, int row, boolean practice) {
        this.col = col;
        this.row = row;
        this.x = (col * SIZE * -1) + ATBPExtension.MAP_SIZE_X;
        this.y =
                (practice ? ATBPExtension.PRAC_MAP_SIZE_Y * -1 : ATBPExtension.MAIN_MAP_SIZE_Y * -1)
                        + (row * SIZE);
    }

    public void run() {}

    public void display(ATBPExtension parentExt, Room room) {
        ExtensionCommands.createActor(
                parentExt,
                room,
                "node" + "col-" + this.col + "row-" + this.row,
                "gnome_a",
                new Point2D.Double(this.x, this.y),
                0f,
                2);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public static Node getNodeAtLocation(ATBPExtension parentExt, Point2D dest, boolean practice) {
        Node[][] nodes = parentExt.getMainMapNodes();
        int maxRow = ATBPExtension.MAX_MAIN_ROW;
        if (practice) {
            nodes = parentExt.getPracticeMapNodes();
            maxRow = ATBPExtension.MAX_PRAC_ROW;
        }
        int likelyCol =
                (int) Math.round(Math.abs((dest.getX() - ATBPExtension.MAP_SIZE_X) / (SIZE * -1)));
        int likelyRow =
                (int)
                        Math.round(
                                (dest.getY()
                                                + (practice
                                                        ? ATBPExtension.PRAC_MAP_SIZE_Y
                                                        : ATBPExtension.MAIN_MAP_SIZE_Y))
                                        / SIZE);
        if (likelyCol < 0) likelyCol = 0;
        if (likelyRow < 0) likelyRow = 0;
        // Console.debugLog("Col: " + likelyCol + " Row: " + likelyRow);
        try {
            return nodes[likelyCol][likelyRow];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public Node getNearbyAvailableNode(ATBPExtension parentExt, boolean practice) {
        Node nodeRight = null;
        Node nodeLeft = null;
        Node nodeUp = null;
        Node nodeDown = null;
        if (this.col + 1 < ATBPExtension.MAX_COL)
            nodeRight =
                    practice
                            ? parentExt.getPracticeMapNodes()[this.col + 1][this.row]
                            : parentExt.getMainMapNodes()[this.col + 1][this.row];
        if (this.col - 1 >= 0)
            nodeLeft =
                    practice
                            ? parentExt.getPracticeMapNodes()[this.col - 1][this.row]
                            : parentExt.getMainMapNodes()[this.col - 1][this.row];
        if (this.row - 1 >= 0)
            nodeUp =
                    practice
                            ? parentExt.getPracticeMapNodes()[this.col][this.row - 1]
                            : parentExt.getMainMapNodes()[this.col][this.row - 1];
        if (this.row + 1 < (practice ? ATBPExtension.MAX_PRAC_ROW : ATBPExtension.MAX_MAIN_ROW))
            nodeDown =
                    practice
                            ? parentExt.getPracticeMapNodes()[this.col][this.row + 1]
                            : parentExt.getMainMapNodes()[this.col][this.row + 1];
        if (nodeRight != null && !nodeRight.isSolid()) return nodeRight;
        if (nodeLeft != null && !nodeLeft.isSolid()) return nodeLeft;
        if (nodeUp != null && !nodeUp.isSolid()) return nodeUp;
        if (nodeDown != null && !nodeDown.isSolid()) return nodeDown;
        return null;
    }

    public int getCol() {
        return this.col;
    }

    public int getRow() {
        return this.row;
    }

    public int getGCost(Node startNode) {
        int xDist = Math.abs(this.col - startNode.getCol());
        int yDist = Math.abs(this.row - startNode.getRow());
        return xDist + yDist;
    }

    public int getHCost(Node goalNode) {
        int xDist = Math.abs(this.col - goalNode.getCol());
        int yDist = Math.abs(this.row - goalNode.getRow());
        return xDist + yDist;
    }

    public void printCosts(Node startNode, Node goalNode) {
        Console.debugLog("Node: x=" + this.x + " y=" + this.y);
        Console.debugLog("GCOST: " + this.getGCost(startNode));
        Console.debugLog("HCOST: " + this.getHCost(goalNode));
        Console.debugLog("FCOST: " + this.getFCost(startNode, goalNode));
    }

    public int getFCost(Node startNode, Node goalNode) {
        return this.getGCost(startNode) + this.getHCost(goalNode);
    }

    public static List<Point2D> getPath(
            ATBPExtension parentExt,
            Node startNode,
            Node currentNode,
            Node goalNode,
            boolean practice) {
        // Console.debugLog("Practice: " + practice);
        Node[][] mapNodes = parentExt.getMainMapNodes();
        int maxRow = ATBPExtension.MAX_MAIN_ROW;
        if (practice) {
            mapNodes = parentExt.getPracticeMapNodes();
            maxRow = ATBPExtension.MAX_PRAC_ROW;
        }
        List<Node> checkedNodes = new ArrayList<>();
        List<Node> openNodes = new ArrayList<>();
        List<Point2D> path = new ArrayList<>();
        Map<Node, Node> trackedNodes = new HashMap<>();
        int step = 0;
        while (step < 300) { // TODO: May lag the server if done unnecessarily. Seems to be an issue
            // when players die sometimes
            if (currentNode == goalNode) {
                Node current = goalNode;
                int test = 0;
                while (current != startNode && test < 200) {
                    test++;
                    path.add(current.getLocation());
                    current = trackedNodes.get(current);
                    // current.display(parentExt, a.getRoom());
                }
                break;
            } else {
                checkedNodes.add(currentNode);
                openNodes.remove(currentNode);
            }
            // Console.debugLog("StartNode: x=" + startNode.getX() + " y=" + startNode.getY());
            // Console.debugLog("CurrentNode: x=" + currentNode.getX() + " y=" +
            // currentNode.getY());
            // Console.debugLog("Solid: " + currentNode.isSolid());
            // Console.debugLog("GoalNode: x=" + goalNode.getX() + " y=" + goalNode.getY());
            // Console.debugLog("===========================");
            if (currentNode.getRow() - 1 >= 0) {
                Node upNode = mapNodes[currentNode.getCol()][currentNode.getRow() - 1];
                if (upNode.canBeOpened(checkedNodes, openNodes)) {
                    openNodes.add(upNode);
                    trackedNodes.put(upNode, currentNode);
                }
            }
            if (currentNode.getCol() - 1 >= 0) {
                Node leftNode = mapNodes[currentNode.getCol() - 1][currentNode.getRow()];
                if (leftNode.canBeOpened(checkedNodes, openNodes)) {
                    openNodes.add(leftNode);
                    trackedNodes.put(leftNode, currentNode);
                }
            }
            if (currentNode.getRow() + 1 < maxRow) {
                Node downNode = mapNodes[currentNode.getCol()][currentNode.getRow() + 1];
                if (downNode.canBeOpened(checkedNodes, openNodes)) {
                    openNodes.add(downNode);
                    trackedNodes.put(downNode, currentNode);
                }
            }
            if (currentNode.getCol() + 1 < ATBPExtension.MAX_COL) {
                Node rightNode = mapNodes[currentNode.getCol() + 1][currentNode.getRow()];
                if (rightNode.canBeOpened(checkedNodes, openNodes)) {
                    openNodes.add(rightNode);
                    trackedNodes.put(rightNode, currentNode);
                }
            }

            int bestNodeIndex = 0;
            int bestNodefCost = 999;

            for (int i = 0; i < openNodes.size(); i++) {
                // Console.debugLog("Best F Cost:" + bestNodefCost);
                // openNodes.get(i).printCosts(startNode, goalNode);
                if (openNodes.get(i).getFCost(startNode, goalNode) < bestNodefCost) {
                    bestNodefCost = openNodes.get(i).getFCost(startNode, goalNode);
                    bestNodeIndex = i;
                } else if (openNodes.get(i).getFCost(startNode, goalNode) == bestNodefCost) {
                    if (openNodes.get(i).getGCost(startNode)
                            < openNodes.get(bestNodeIndex).getGCost(startNode)) {
                        bestNodeIndex = i;
                    }
                }
            }
            if (openNodes.size() == 0) {
                return null;
            }
            currentNode = openNodes.get(bestNodeIndex);
            // Console.debugLog("NewNode: x=" + currentNode.getX() + " y=" + currentNode.getY());
            step++;
        }
        Collections.reverse(path);
        return path;
    }

    private static double ccw(Point2D a, Point2D b, Point2D c) {
        Console.debugLog(
                (b.getX() - a.getX()) * (c.getY() - a.getY())
                        - (b.getY() - a.getY()) * (c.getX() - a.getX()));
        return (b.getX() - a.getX()) * (c.getY() - a.getY())
                - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    public Point2D getLocation() {
        return new Point2D.Double(this.x, this.y);
    }

    public boolean canBeOpened(List<Node> checkedNodes, List<Node> openNodes) {
        return !openNodes.contains(this) && !checkedNodes.contains(this) && !this.solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return this.solid;
    }
}
