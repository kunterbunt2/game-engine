/*
 * Copyright (C) 2024 Abdalla Bushnaq
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

package de.bushnaq.abdalla.engine.physics;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import de.bushnaq.abdalla.engine.GameObject;
import de.bushnaq.abdalla.engine.RenderEngineExtension;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhysicsEngine<T extends RenderEngineExtension> {
    public final static short ALL_FLAG    = -1;
    public final static short BOX_FLAG    = 1 << 8;
    public final static short MARBLE_FLAG = 1 << 9;
    btCollisionAlgorithm     algorithm;
    btBroadphaseInterface    broadphase;
    btCollisionConfiguration collisionConfig;
    public btCollisionWorld collisionWorld;
    MyContactListener    contactListener;
    DebugDrawer          debugDrawer;
    btDispatcher         dispatcher;
    Array<GameObject<T>> instances = new Array<>();
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void add(GameObject<T> gameObject, btCollisionObject collisionObject, short marbleFlag, short allFlag) {
        collisionObject.setUserValue(instances.size);
        instances.add(gameObject);
        collisionObject.setCollisionFlags(collisionObject.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        collisionWorld.addCollisionObject(collisionObject/*, PhysicsEngine.MARBLE_FLAG, PhysicsEngine.ALL_FLAG*/);
    }

    public void create() {
        Bullet.init();
        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher      = new btCollisionDispatcher(collisionConfig);
        broadphase      = new btDbvtBroadphase();
        collisionWorld  = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
        collisionWorld.setDebugDrawer(debugDrawer);
        contactListener = new MyContactListener();
//        btCollisionAlgorithmConstructionInfo ci = new btCollisionAlgorithmConstructionInfo();
//        ci.setDispatcher1(dispatcher);
//            algorithm = new btSphereBoxCollisionAlgorithm(null, ci, co0.wrapper, co1.wrapper, false);
    }

    public void dispose() {
        collisionWorld.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfig.dispose();
        contactListener.dispose();
    }

    public void remove(GameObject<T> gameObject, btCollisionObject ballObject) {
        instances.removeValue(gameObject, false);
        collisionWorld.removeCollisionObject(ballObject);
    }

    public void render(MovingCamera camera) {
//        logger.info(String.format("testing %d objects collisions...", collisionWorld.getNumCollisionObjects()));
        collisionWorld.performDiscreteCollisionDetection();
        debugDrawer.begin(camera);
        collisionWorld.debugDrawWorld();
        debugDrawer.end();
    }

    class MyContactListener extends ContactListener {
        @Override
        public boolean onContactAdded(int userValue0, int partId0, int index0, int userValue1, int partId1, int index1) {
            GameObject<T> go0 = instances.get(userValue0);
            GameObject<T> go1 = instances.get(userValue1);
            logger.info("contact!");
            return true;
        }
    }
}
