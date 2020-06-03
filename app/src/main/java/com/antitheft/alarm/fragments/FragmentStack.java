package com.antitheft.alarm.fragments;

import com.antitheft.alarm.utils.Log;

import java.util.Stack;

public class FragmentStack {
    private Stack<Integer> fragmentIds;
    private static FragmentStack instance;

    private FragmentStack() {
        fragmentIds = new Stack<>();
        fragmentIds.clear();
    }

    public static FragmentStack getInstance() {
        if (instance == null) {
            synchronized (FragmentStack.class) {
                if (instance == null) {
                    instance = new FragmentStack();
                }
            }
        }
        return instance;
    }

    public void push(int id) {
        if (fragmentIds.contains(id)) {
            Log.i("FragmentStack remove: " + id);
            fragmentIds.remove((Integer) id);
        }
        Log.i("FragmentStack push: " + id);
        fragmentIds.push(id);
    }

    public void pop() {
        int id = fragmentIds.pop();
        Log.i("FragmentStack pop: " + id);
    }

    public int getTop() {
        int id = fragmentIds.peek();
        Log.i("FragmentStack getTop: " + id);
        return id;
    }

    public int size() {
        int size = fragmentIds.size();
        Log.i("FragmentStack size: " + size);
        return size;
    }

    public void clear() {
        fragmentIds.clear();
    }
}

