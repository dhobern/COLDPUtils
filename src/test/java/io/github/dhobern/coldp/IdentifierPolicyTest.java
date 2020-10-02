/*
 * Copyright 2020 dhobern@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.IdentifierPolicy.IdentifierType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dhobern@gmail.com
 */
public class IdentifierPolicyTest {
    
    private IdentifierPolicy policy;;
    
    public IdentifierPolicyTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        policy = new IdentifierPolicy();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of processInstance method, of class IdentifierPolicy.
     */
    @Test
    public void testProcessInstanceMixed() {
        policy.processInstance("1");
        policy.processInstance("a");
        assertEquals(IdentifierType.String, policy.getType());
        assertEquals("10001", policy.nextIdentifier());
    }

    @Test
    public void testProcessInstanceAllUUIDs() {
        policy.processInstance("123e4567-e89b-12d3-a456-556642440000");
        policy.processInstance("123e4567-e89b-12d3-a456-556642440001");
        assertEquals(IdentifierType.UUID, policy.getType());
        assertTrue(policy.nextIdentifier().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    public void testProcessInstanceAllInts() {
        policy.processInstance("1");
        policy.processInstance("2");
        assertEquals(IdentifierType.Int, policy.getType());
        assertEquals("3", policy.nextIdentifier());
    }   

    @Test
    public void testProcessInstanceNoExemplars() {
        assertEquals(IdentifierType.Int, policy.getType());
        assertEquals("1", policy.nextIdentifier());
    }   
}
