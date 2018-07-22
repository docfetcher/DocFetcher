/*******************************************************************************
 * Copyright (c) 2018 Zhengmian Hu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Zhengmian Hu - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher;

import net.sourceforge.docfetcher.enums.ProgramConf;
import py4j.GatewayServer;

/**
 * Created by huzhengmian on 2018/5/5.
 */
public class Py4jHandler {

    private static GatewayServer server;
    private static synchronized GatewayServer getServer(){
        if(server==null){
            server = new GatewayServer(new Py4jHandler(), ProgramConf.Int.PythonApiPort.get());
        }
        return server;
    }
    public static void openGatewayServer(){
        getServer().start();
    }
    public static void shutdownGatewayServer(){
        getServer().shutdown();
    }
}
