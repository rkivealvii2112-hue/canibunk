package com.canibunk.service;

import com.canibunk.ds.AttendanceBST;
import com.canibunk.model.Subject;
import java.util.Collection;
import java.util.List;

/**
 * Service that uses the Binary Search Tree (AttendanceBST) for Danger Sorting.
 * In-Order traversal extracts subjects ordered from lowest percentage to highest.
 */
public class DangerZoneService {
    private final AttendanceBST bst;

    public DangerZoneService() {
        this.bst = new AttendanceBST();
    }

    public void rebuildTree(Collection<Subject> subjects) {
        bst.clear();
        for (Subject s : subjects) {
            bst.insert(s);
        }
    }

    public List<Subject> getSortedSubjects() {
        return bst.inOrderTraversal();
    }

    public List<Subject> getCriticalSubjects(double threshold) {
        return bst.getDangerZoneSubjects(threshold);
    }

    public Subject getMostCriticalSubject() {
        return bst.getMostCriticalSubject();
    }

    public Subject getSafestSubject() {
        return bst.getSafestSubject();
    }

    public AttendanceBST getBST() {
        return bst;
    }
}
