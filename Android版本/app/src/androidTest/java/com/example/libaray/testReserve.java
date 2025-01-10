package com.example.libaray;


import static org.junit.Assert.assertTrue;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.libaray.utilize.Reserve;
import org.junit.Assert.*;
public class testReserve {
    @Test
    public void test() {
        Reserve reserve = new Reserve(InstrumentationRegistry.getInstrumentation().getTargetContext());
        assertTrue(reserve.signIn("202111050676","082416"));
    }
}
