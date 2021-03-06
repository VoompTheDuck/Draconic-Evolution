package com.brandon3055.draconicevolution.client.render.shaders;

import codechicken.lib.render.shader.ShaderObject;

import java.io.IOException;

import static codechicken.lib.render.shader.ShaderHelper.getStream;
import static codechicken.lib.render.shader.ShaderHelper.readShader;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.VERTEX;

/**
 * Created by brandon3055 on 6/11/2016.
 */
public class DEShaders {

    public static ShaderObject reactor;
    public static ShaderObject reactorShield;

    public static ShaderObject reactorBeamI;
    public static ShaderObject reactorBeamO;
    public static ShaderObject reactorBeamE;

    public static ShaderObject energyCrystal_V;
    public static ShaderObject energyCrystal_F;

    public static ShaderObject explosionOverlay;

    public static ShaderObject explosionBlastWave;
    public static ShaderObject explosionLeadingWave;
    public static ShaderObject explosionCoreEffect;

    static {
        if (useShaders()) {
            try {
                initShaders();
            } catch (IOException e) {
                throw new RuntimeException("Unable to initialize DEShaders.", e);
            }
        }
    }

    //Used in dev to reload shaders runtime
    public static void initShaders() throws IOException {
        dispose(reactor);
        dispose(reactorShield);
        dispose(reactorBeamI);
        dispose(reactorBeamO);
        dispose(reactorBeamE);
        dispose(energyCrystal_V);
        dispose(explosionOverlay);
        dispose(explosionBlastWave);
        dispose(explosionLeadingWave);
        dispose(explosionCoreEffect);

        //reactorOp
        reactor = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/reactor.frag")));
        reactorShield = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/reactor_shield.frag")));

        //reactorBeamOp
        reactorBeamI = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/reactor_beam_i.frag")));
        reactorBeamO = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/reactor_beam_o.frag")));
        reactorBeamE = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/reactor_beam_e.frag")));

        //eCrystalOp
        energyCrystal_V = new ShaderObject(VERTEX, readShader(getStream("/assets/draconicevolution/shaders/energy_crystal.vert")));
        energyCrystal_F = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/energy_crystal.frag")));

        //explosionOverlayOp
        explosionOverlay = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/explosion_overlay.frag")));

        //explosionWaveOp
        explosionBlastWave = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/explosion_blast_wave.frag")));
        explosionLeadingWave = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/explosion_leading_wave.frag")));
        explosionCoreEffect = new ShaderObject(FRAGMENT, readShader(getStream("/assets/draconicevolution/shaders/explosion_core_effect.frag")));
    }

    public static boolean useShaders() { //TODO Shader stuff?
        return true;//OpenGlHelper.shadersSupported && BCConfigOld.useShaders;
    }

    private static void dispose(ShaderObject object) {
        if (object != null) {
            object.disposeObject();
        }
    }
}
