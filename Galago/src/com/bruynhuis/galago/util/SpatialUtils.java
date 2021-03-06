/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.util;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;

/**
 *
 * This is a spatial utility class which can be used to create or convert
 * certain spatial parameters.
 *
 * @author nidebruyn
 */
public class SpatialUtils {

    /**
     * 
     * @param parent
     * @param type
     * @return 
     */
    public static Spatial addSkySphere(Node parent, int type) {
        String texture = "Resources/sky/day.jpg";

        if (type == 2) {
            texture = "Resources/sky/cloudy.jpg";
        } else if (type == 3) {
            texture = "Resources/sky/night.jpg";
        }
        
        Texture t = SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture(texture);

        Spatial sky = SkyFactory.createSky(SharedSystem.getInstance().getBaseApplication().getAssetManager(), t, true);
        parent.attachChild(sky);
        sky.setLocalScale(1);
        return sky;

    }

    /**
     * Add some real simple water to the scene.
     * @param parent
     * @param size
     * @param yPos
     * @param waveSpeed
     * @param waterDepth
     * @return 
     */
    public static SimpleWaterProcessor addSimpleWater(Node parent, float size, float yPos, float waveSpeed, float waterDepth) {
        //create processor
        SimpleWaterProcessor simpleWaterProcessor = new SimpleWaterProcessor(SharedSystem.getInstance().getBaseApplication().getAssetManager());
        simpleWaterProcessor.setReflectionScene(parent);
//        simpleWaterProcessor.setDistortionMix(0.5f);
//        simpleWaterProcessor.setDistortionScale(0.15f);
        simpleWaterProcessor.setWaveSpeed(waveSpeed);
        simpleWaterProcessor.setWaterDepth(waterDepth);
        simpleWaterProcessor.setWaterTransparency(0.15f);
        simpleWaterProcessor.setTexScale(2);
        simpleWaterProcessor.setWaterColor(ColorRGBA.Blue);
        SharedSystem.getInstance().getBaseApplication().getViewPort().addProcessor(simpleWaterProcessor);
//        simpleWaterProcessor.setLightPosition(new Vector3f(30, 30, -30));

        //create water quad
        Geometry waterPlane = simpleWaterProcessor.createWaterGeometry(size, size);
        waterPlane.setMaterial(simpleWaterProcessor.getMaterial());
        waterPlane.setLocalTranslation(-size * 0.5f, 0.5f, size * 0.5f);

        parent.attachChild(waterPlane);

        return simpleWaterProcessor;

    }

    /**
     * Add more complex water.
     * @param parent
     * @param lightDir
     * @param yPos
     * @return 
     */
    public static FilterPostProcessor addOceanWater(Node parent, Vector3f lightDir, float yPos) {
        FilterPostProcessor fpp = new FilterPostProcessor(SharedSystem.getInstance().getBaseApplication().getAssetManager());
        final WaterFilter water = new WaterFilter(parent, lightDir);
        water.setWaterHeight(yPos);
        water.setWindDirection(new Vector2f(-0.15f, 0.15f));
        water.setFoamHardness(0.85f);
        water.setFoamExistence(new Vector3f(0.2f, 1f, 0.6f));
        water.setShoreHardness(0.5f);
        water.setMaxAmplitude(0.5f);
        water.setWaveScale(0.01f);
        water.setSpeed(0.9f);
        water.setShininess(0.1f);
        water.setNormalScale(0.75f);
//        water.setRefractionConstant(0.2f);
//        water.setReflectionDisplace(20f);
        water.setCausticsIntensity(0.8f);
        water.setWaterTransparency(1.2f);
        water.setColorExtinction(new Vector3f(10f, 20f, 30f));
        fpp.addFilter(water);
        SharedSystem.getInstance().getBaseApplication().getViewPort().addProcessor(fpp);
        return fpp;
    }

    /**
     * Convert the terrain to an unshaded terrain.
     * This is for use on android and slow devices.
     * 
     * @param terrainQuad 
     */
    public static void makeTerrainUnshaded(TerrainQuad terrainQuad) {
        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if (spatial instanceof Geometry) {
                    Geometry geom = (Geometry) spatial;
                    
                    Material mat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");
                    mat.setBoolean("useTriPlanarMapping", false);
                    mat.setTexture("Alpha", geom.getMaterial().getTextureParam("AlphaMap").getTextureValue());

                    if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {
                        mat.setTexture("Tex1", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                        mat.getTextureParam("Tex1").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex1Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_0_scale").getValueAsString()));
                    }

                    if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {
                        mat.setTexture("Tex2", geom.getMaterial().getTextureParam("DiffuseMap_1").getTextureValue());
                        mat.getTextureParam("Tex2").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex2Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_1_scale").getValueAsString()));
                    }

                    if (geom.getMaterial().getTextureParam("DiffuseMap_2") != null) {
                        mat.setTexture("Tex3", geom.getMaterial().getTextureParam("DiffuseMap_2").getTextureValue());
                        mat.getTextureParam("Tex3").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex3Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_2_scale").getValueAsString()));
                    }

                    geom.setMaterial(mat);

                }
            }
        };
        terrainQuad.depthFirstTraversal(sgv);
    }

    /**
     * Helper method which converts all ligting materials of a node to an unshaded material.
     * @param node 
     */
    public static void makeUnshaded(Node node) {

        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {

                if (spatial instanceof Geometry) {

                    Geometry geom = (Geometry) spatial;
                    Material mat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    Material tat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");

                    if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {

                        tat.setTexture("Alpha", geom.getMaterial().getTextureParam("AlphaMap").getTextureValue());

                        if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {

                            tat.setTexture("Tex1", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                            tat.getTextureParam("Tex1").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex1Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_0_scale").getValueAsString()));

                        }

                        if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {

                            tat.setTexture("Tex2", geom.getMaterial().getTextureParam("DiffuseMap_1").getTextureValue());
                            tat.getTextureParam("Tex2").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex2Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_1_scale").getValueAsString()));

                        }

                        if (geom.getMaterial().getTextureParam("DiffuseMap_2") != null) {

                            tat.setTexture("Tex3", geom.getMaterial().getTextureParam("DiffuseMap_2").getTextureValue());
                            tat.getTextureParam("Tex3").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex3Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_2_scale").getValueAsString()));

                        }

                        tat.setBoolean("useTriPlanarMapping", true);
                        geom.setMaterial(tat);

                    } else if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {

                        mat.setTexture("ColorMap", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                        mat.setFloat("AlphaDiscardThreshold", .5f);
                        mat.setFloat("ShadowIntensity", 5);
                        mat.setVector3("LightPos", new Vector3f(5, 20, 5));
                        geom.setMaterial(mat);

                    }

                }

            }
        };

        node.depthFirstTraversal(sgv);

    }
}
