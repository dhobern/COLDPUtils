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
public class COLDataPackageTest {
    
    private COLDataPackage coldp; 
    
    public COLDataPackageTest() {
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

    @Test
    public void testNameToReference() {
        COLDPName name = coldp.getNames().get("271"); // Ochyrotica fasciata
        COLDPReference reference = name.getReference(); // New genera of Agdistidae and Pterophoridae
        
        assertEquals(reference.getTitle(), "New genera of Agdistidae and Pterophoridae");
    }

    @Test
    public void testNameRender() {
        COLDPName name = coldp.getNames().get("271"); // Ochyrotica fasciata
        
        name.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None, "--> ", 3));
    }
    
    @Test
    public void testTaxonRender() {
        COLDPTaxon taxon = coldp.getTaxa().get("268"); // Ochyroticinae
        
        taxon.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None));
    }

    @Test
    public void testDeleteNameReference() {
        int count = coldp.getNameReferences().size();
        
        COLDPName name = coldp.getNames().get("3060"); // Agdistopis
        assertEquals(1, name.getNameReferences().size());
        
        COLDPNameReference nr = name.getNameReferences().iterator().next();
        COLDPReference reference = nr.getReference();
        assertEquals("813", reference.getID());
        assertEquals(2, reference.getNameReferences().size());
        
        assertTrue(coldp.deleteNameReference(nr));
        
        assertEquals(0, name.getNameReferences().size());
        assertEquals(1, reference.getNameReferences().size());
        assertEquals(count - 1, coldp.getNameReferences().size());
    }

    @Test
    public void testRegionToTaxa() {
        List<COLDPTaxon> expected = new ArrayList<>(); 
        expected.add(coldp.getTaxa().get("271"));
        expected.add(coldp.getTaxa().get("275"));
        expected.add(coldp.getTaxa().get("277"));

        COLDPRegion region = coldp.getRegions().get("PA"); // Paraguay
        Set<COLDPDistribution> distributions = region.getDistributions();
        assertEquals(3, distributions.size());
        for (COLDPDistribution distribution :  distributions) {
            assert(expected.contains(distribution.getTaxon()));
        }
    }
    
    
    @Test
    public void testCOLDP() {
        for (COLDPName name: coldp.getNames().values()) {
            System.out.println(name.getID() + ", " + name.getScientificName() + ": " 
                    + "B: " + (name.getBasionym() == null ? 0 : name.getBasionym().getID())  + " / " 
                    + "R: " + (name.getReference() == null ? 0 : name.getReference().getID()) + " / " 
                    + "NRefs: " + (name.getNameReferences() == null ? -1 : name.getNameReferences().size()) + " / "
                    + "NRSubjs: " + (name.getNameRelations() == null ? -1 : name.getNameRelations().size()) + " / "
                    + "NRObjss: " + (name.getRelatedNameRelations() == null ? -1 : name.getRelatedNameRelations().size()) + " / "
                    + "T: " + (name.getTaxon() == null ? 0 : name.getTaxon().getID()) + " / "
                    + "Syns: " + (name.getSynonyms() == null ? -1 : name.getSynonyms().size()));
        }
        
        for (COLDPRegion region : coldp.getRegions().values()) {
            if (region.getDistributions() != null) {
                System.out.println(region.getName());
                for (COLDPDistribution distribution : region.getDistributions()) {
                    System.out.println("    " + distribution.getTaxon().getName().getScientificName());
                }
            }
        }
        
        int i = 1;
        for (COLDPName name : coldp.getNames().values()) {
            name.setID(String.valueOf(i++));
        }
        
        i = 1;
        for (COLDPReference reference : coldp.getReferences().values()) {
            reference.setID(String.valueOf(i++));
        }
        
        i = 1;
        for (COLDPTaxon taxon : coldp.getTaxa().values()) {
            taxon.setID(String.valueOf(i++));
        }
        
        coldp.write("mockdata", "-NEW");
        
    }
    
    @Test
    public void testNewRecords() {
        COLDPName name = coldp.getNames().get("271"); // Ochyrotica fasciata
        COLDPReference reference = name.getReference(); // New genera of Agdistidae and Pterophoridae

        for (int i = 0; i < 5; i++) {
            COLDPNameReference nr = coldp.newNameReference();
            nr.setName(name);
            nr.setReference(reference);
            nr.setPage(String.valueOf(i));
            nr.setRemarks("Whatever is on page " + i);
        }
        
        assertEquals(5, name.getNameReferences().size());
        assertEquals(5, reference.getNameReferences().size());

        name.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None, "  ", 0));
        
        COLDPTaxon Ochyrotica = name.getTaxon().getParent();
        COLDPTaxon falsa = coldp.newTaxon();
        falsa.setParent(Ochyrotica);
    }
    
    @Test
    public void testAddName() {
        COLDPTaxon Ochyrotica = coldp.getTaxa().get("268");
        int childCount = Ochyrotica.getChildren().size();
        
        COLDPName nova = coldp.addName(RankEnum.species, "Ochyrotica", "nova", null, "Dylan, 1963", null, null, Ochyrotica,
                      coldp.getReferences().get("813"), "12", "https://hobern.net/", "Not a real species", "established", 
                      "accepted", "Still not a good species", "Donald Hobern");
        coldp.addName(RankEnum.species, "Ochyrotica", "malissima", null, "Tengo, 2000", null, nova.getTaxon(), null,
                      coldp.getReferences().get("1905"), "122", "https://hobern.net/", "Very much not a real species", "established", 
                      "synonym", "Not in any way a good species", "Donald Hobern");
        
        assertEquals(childCount + 1, Ochyrotica.getChildren().size());
        assertEquals(1, nova.getTaxon().getSynonyms().size());
        
        System.out.println(nova.toCSV());
        System.out.println(nova.getTaxon().getSynonyms().iterator().next().getName().toCSV());
        
        Ochyrotica.render(new PrintWriter(System.out, true), new TreeRenderProperties(TreeRenderType.HTML, ContextType.None, "  ", 0));

    }
}