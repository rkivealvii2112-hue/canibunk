package com.canibunk.ds;

import com.canibunk.model.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Binary Search Tree (BST) for Danger Sorting of subjects.
 * Ordered by Subject Attendance Percentage (ascending).
 * 
 * In-Order Traversal (Left -> Root -> Right) naturally visits subjects from
 * lowest attendance % (Most in Danger) to highest attendance % (Safest).
 */
public class AttendanceBST {

    public static class BSTNode {
        private Subject data;
        private BSTNode left;
        private BSTNode right;

        public BSTNode(Subject data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }

        public Subject getData() {
            return data;
        }

        public BSTNode getLeft() {
            return left;
        }

        public BSTNode getRight() {
            return right;
        }
    }

    private BSTNode root;
    private int nodeCount;

    public AttendanceBST() {
        this.root = null;
        this.nodeCount = 0;
    }

    /**
     * Inserts a subject into the BST based on attendance percentage.
     * Tie-breaker uses subjectCode.
     * Time Complexity: O(h), where h is tree height (O(log n) average).
     */
    public void insert(Subject subject) {
        if (subject == null) return;
        root = insertRec(root, subject);
        nodeCount++;
    }

    private BSTNode insertRec(BSTNode current, Subject subject) {
        if (current == null) {
            return new BSTNode(subject);
        }

        int cmp = subject.compareTo(current.data);
        if (cmp < 0) {
            current.left = insertRec(current.left, subject);
        } else {
            // >= goes right so duplicate percentages are preserved
            current.right = insertRec(current.right, subject);
        }
        return current;
    }

    /**
     * In-Order Traversal (Left -> Root -> Right).
     * Returns subjects sorted from lowest attendance % to highest.
     */
    public List<Subject> inOrderTraversal() {
        List<Subject> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    private void inOrderRec(BSTNode node, List<Subject> list) {
        if (node != null) {
            inOrderRec(node.left, list);
            list.add(node.data);
            inOrderRec(node.right, list);
        }
    }

    /**
     * Retrieves all subjects whose attendance percentage is below the threshold.
     */
    public List<Subject> getDangerZoneSubjects(double threshold) {
        List<Subject> result = new ArrayList<>();
        collectDangerZone(root, threshold, result);
        return result;
    }

    private void collectDangerZone(BSTNode node, double threshold, List<Subject> result) {
        if (node == null) return;
        collectDangerZone(node.left, threshold, result);
        if (node.data.getAttendancePercentage() < threshold) {
            result.add(node.data);
            collectDangerZone(node.right, threshold, result);
        }
    }

    /**
     * Returns the subject with the absolute lowest attendance percentage (leftmost node).
     */
    public Subject getMostCriticalSubject() {
        if (root == null) return null;
        BSTNode curr = root;
        while (curr.left != null) {
            curr = curr.left;
        }
        return curr.data;
    }

    /**
     * Returns the subject with the highest attendance percentage (rightmost node).
     */
    public Subject getSafestSubject() {
        if (root == null) return null;
        BSTNode curr = root;
        while (curr.right != null) {
            curr = curr.right;
        }
        return curr.data;
    }

    public int size() {
        return nodeCount;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
        nodeCount = 0;
    }

    /**
     * Prints an ASCII visualization of the BST.
     */
    public void printTree() {
        printTreeRec(root, "", true);
    }

    private void printTreeRec(BSTNode node, String prefix, boolean isTail) {
        if (node != null) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") 
                    + String.format("%s: %.2f%% (%s)", 
                        node.data.getSubjectCode(), 
                        node.data.getAttendancePercentage(),
                        node.data.getSubjectName()));
            
            boolean hasLeft = (node.left != null);
            boolean hasRight = (node.right != null);

            if (hasLeft || hasRight) {
                if (node.left != null) {
                    printTreeRec(node.left, prefix + (isTail ? "    " : "│   "), !hasRight);
                }
                if (node.right != null) {
                    printTreeRec(node.right, prefix + (isTail ? "    " : "│   "), true);
                }
            }
        }
    }
}
