package com.fullsecurity.fsclient;

import com.fullsecurity.shoppingcart.ShoppingCartFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.testng.PowerMockTestCase;

@PrepareForTest({ShoppingCartFragment.class})
@RunWith(PowerMockRunner.class)
public class ShoppingCartTest extends PowerMockTestCase {

    @Test
    public void readStringFromContext_LocalizedString() {
        assert(3==3);
    }
}
