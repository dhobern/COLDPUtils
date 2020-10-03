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

import java.io.PrintWriter;
import java.io.StringWriter;
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
public class COLDPTaxonTest {

    private COLDataPackage coldp;
    
    public COLDPTaxonTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        coldp = new COLDataPackage("mockdata");
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of fixHierarchy method, of class COLDPTaxon.
     */
    @Test
    public void testFixHierarchy() {
        COLDPTaxon fasciata = coldp.getTaxa().get("271");
        COLDPTaxon Ochyrotica = fasciata.getParent();
        COLDPTaxon Agdistopis = coldp.getTaxa().get("3060");
        COLDPTaxon Pterophoroidea = coldp.getTaxa().get("3836");
        
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        
        Pterophoroidea.render(writer, 
           new TreeRenderProperties(TreeRenderProperties.TreeRenderType.HTML, 
                                    TreeRenderProperties.ContextType.None));

        String before = out.toString();
        
        int ochyroticaChildren = Ochyrotica.getChildren().size();
        int agdistopisChildren = Agdistopis.getChildren().size();
        int pterophoroideaChildren = Pterophoroidea.getChildren().size();
        
        fasciata.setParent(Agdistopis);
        
        assertEquals(ochyroticaChildren - 1, Ochyrotica.getChildren().size());
        assertEquals(agdistopisChildren + 1, Agdistopis.getChildren().size());
        assertEquals(pterophoroideaChildren, Pterophoroidea.getChildren().size());

        fasciata.fixHierarchy(true);
        
        assertEquals("Agdistopis fasciata", fasciata.getName().getScientificName());
        
        assertEquals(Agdistopis, fasciata.getParent());
        assertEquals(Agdistopis.getKingdom(), fasciata.getKingdom());
        assertEquals(Agdistopis.getPhylum(), fasciata.getPhylum());
        assertEquals(Agdistopis.getClazz(), fasciata.getClazz());
        assertEquals(Agdistopis.getOrder(), fasciata.getOrder());
        assertEquals(Agdistopis.getSuperfamily(), fasciata.getSuperfamily());
        assertEquals(Agdistopis.getFamily(), fasciata.getFamily());
        assertEquals(Agdistopis.getSubfamily(), fasciata.getSubfamily());
        assertEquals(Agdistopis.getTribe(), fasciata.getTribe());
        assertEquals("Agdistopis", fasciata.getGenus());
        
        out = new StringWriter();
        writer = new PrintWriter(out);
        
        Pterophoroidea.render(writer, 
           new TreeRenderProperties(TreeRenderProperties.TreeRenderType.HTML, 
                                    TreeRenderProperties.ContextType.None));

        String after = out.toString();
        
        assertNotEquals(before, after);

        fasciata.setParent(Pterophoroidea);
        
        assertEquals(ochyroticaChildren - 1, Ochyrotica.getChildren().size());
        assertEquals(agdistopisChildren, Agdistopis.getChildren().size());
        assertEquals(pterophoroideaChildren + 1, Pterophoroidea.getChildren().size());

        fasciata.fixHierarchy(true);
        
        assertEquals("<Unknown genus> fasciata", fasciata.getName().getScientificName());

        assertEquals(Pterophoroidea, fasciata.getParent());
        assertEquals(Pterophoroidea.getKingdom(), fasciata.getKingdom());
        assertEquals(Pterophoroidea.getPhylum(), fasciata.getPhylum());
        assertEquals(Pterophoroidea.getClazz(), fasciata.getClazz());
        assertEquals(Pterophoroidea.getOrder(), fasciata.getOrder());
        assertEquals(Pterophoroidea.getSuperfamily(), fasciata.getSuperfamily());
        assertEquals(Pterophoroidea.getFamily(), fasciata.getFamily());
        assertEquals(Pterophoroidea.getSubfamily(), fasciata.getSubfamily());
        assertEquals(Pterophoroidea.getTribe(), fasciata.getTribe());
        assertEquals("<Unknown genus>", fasciata.getGenus());

        out = new StringWriter();
        writer = new PrintWriter(out);
        
        Pterophoroidea.render(writer, 
           new TreeRenderProperties(TreeRenderProperties.TreeRenderType.HTML, 
                                    TreeRenderProperties.ContextType.None));

        after = out.toString();
        
        System.err.println(after);
        
        assertNotEquals(before, after);
        fasciata.setParent(Ochyrotica);
        
        assertEquals(ochyroticaChildren, Ochyrotica.getChildren().size());
        assertEquals(agdistopisChildren, Agdistopis.getChildren().size());
        assertEquals(pterophoroideaChildren, Pterophoroidea.getChildren().size());

        fasciata.fixHierarchy(true);
        
        assertEquals("Ochyrotica fasciata", fasciata.getName().getScientificName());

        assertEquals(Ochyrotica, fasciata.getParent());
        assertEquals(Ochyrotica.getKingdom(), fasciata.getKingdom());
        assertEquals(Ochyrotica.getPhylum(), fasciata.getPhylum());
        assertEquals(Ochyrotica.getClazz(), fasciata.getClazz());
        assertEquals(Ochyrotica.getOrder(), fasciata.getOrder());
        assertEquals(Ochyrotica.getSuperfamily(), fasciata.getSuperfamily());
        assertEquals(Ochyrotica.getFamily(), fasciata.getFamily());
        assertEquals(Ochyrotica.getSubfamily(), fasciata.getSubfamily());
        assertEquals(Ochyrotica.getTribe(), fasciata.getTribe());
        assertEquals("Ochyrotica", fasciata.getGenus());

        out = new StringWriter();
        writer = new PrintWriter(out);
        
        Pterophoroidea.render(writer, 
           new TreeRenderProperties(TreeRenderProperties.TreeRenderType.HTML, 
                                    TreeRenderProperties.ContextType.None));

        after = out.toString();
        
        assertEquals(before, after);
    }
}
