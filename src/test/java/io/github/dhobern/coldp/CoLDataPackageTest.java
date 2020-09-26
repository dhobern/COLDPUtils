/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stang
 */
public class CoLDataPackageTest {
    
    private CoLDataPackage coldp; 
    
    public CoLDataPackageTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        coldp = new CoLDataPackage("mockdata");
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testNameToReference() {
        CoLDPName name = coldp.getNames().get(271); // Ochyrotica fasciata
        CoLDPReference reference = name.getReference(); // New genera of Agdistidae and Pterophoridae
        
        assertEquals(reference.getTitle(), "New genera of Agdistidae and Pterophoridae");
    }

    @Test
    public void testNameRender() {
        CoLDPName name = coldp.getNames().get(271); // Ochyrotica fasciata
        
        name.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None, "--> ", 3));
    }
    
    @Test
    public void testTaxonRender() {
        CoLDPTaxon taxon = coldp.getTaxa().get(268); // Ochyroticinae
        
        taxon.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None));
    }

    @Test
    public void testRegionToTaxa() {
        List<CoLDPTaxon> expected = new ArrayList<>(); 
        expected.add(coldp.getTaxa().get(271));
        expected.add(coldp.getTaxa().get(275));
        expected.add(coldp.getTaxa().get(277));

        CoLDPRegion region = coldp.getRegions().get("PA"); // Paraguay
        Set<CoLDPDistribution> distributions = region.getDistributions();
        assertEquals(3, distributions.size());
        for (CoLDPDistribution distribution :  distributions) {
            assert(expected.contains(distribution.getTaxon()));
        }
    }
    
    
    @Test
    public void testSomeMethod() {
        for (CoLDPName name: coldp.getNames().values()) {
            System.out.println(name.getID() + ", " + name.getScientificName() + ": " 
                    + "B: " + (name.getBasionym() == null ? 0 : name.getBasionym().getID())  + " / " 
                    + "R: " + (name.getReference() == null ? 0 : name.getReference().getID()) + " / " 
                    + "NRefs: " + (name.getNameReferences() == null ? -1 : name.getNameReferences().size()) + " / "
                    + "NRSubjs: " + (name.getNameRelations() == null ? -1 : name.getNameRelations().size()) + " / "
                    + "NRObjss: " + (name.getRelatedNameRelations() == null ? -1 : name.getRelatedNameRelations().size()) + " / "
                    + "T: " + (name.getTaxon() == null ? 0 : name.getTaxon().getID()) + " / "
                    + "Syns: " + (name.getSynonyms() == null ? -1 : name.getSynonyms().size()));
        }
        
        for (CoLDPRegion region : coldp.getRegions().values()) {
            if (region.getDistributions() != null) {
                System.out.println(region.getName());
                for (CoLDPDistribution distribution : region.getDistributions()) {
                    System.out.println("    " + distribution.getTaxon().getName().getScientificName());
                }
            }
        }
        
        int i = 1;
        for (CoLDPName name : coldp.getNames().values()) {
            name.setID(i++);
        }
        
        i = 1;
        for (CoLDPReference reference : coldp.getReferences().values()) {
            reference.setID(i++);
        }
        
        i = 1;
        for (CoLDPTaxon taxon : coldp.getTaxa().values()) {
            taxon.setID(i++);
        }
        
        coldp.write("mockdata");
        
    }
    
}
