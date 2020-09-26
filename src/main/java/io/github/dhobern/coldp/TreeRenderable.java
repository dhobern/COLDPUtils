/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.io.PrintWriter;

/**
 *
 * @author stang
 */
public interface TreeRenderable {
    public void render(PrintWriter writer, TreeRenderProperties context);
}
