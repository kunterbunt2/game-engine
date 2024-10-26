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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import de.bushnaq.abdalla.engine.GameObject;
import de.bushnaq.abdalla.engine.IGameEngine;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhysicsEngine<T extends IGameEngine> {
    public final static short                               ALL_FLAG    = -1;
    public final static short                               BOX_FLAG    = 1 << 8;
    public final static short                               MARBLE_FLAG = 1 << 9;
    private             btCollisionAlgorithm                algorithm;
    private             btBroadphaseInterface               broadphase;
    private             btCollisionConfiguration            collisionConfig;
    private             btSequentialImpulseConstraintSolver constraintSolver;
    private final       boolean                             debug       = false;
    private             DebugDrawer                         debugDrawer;
    private             btDispatcher                        dispatcher;
    private             btDiscreteDynamicsWorld             dynamicsWorld;
    public              Array<Object>                       instances   = new Array<>();
    protected           Logger                              logger      = LoggerFactory.getLogger(this.getClass());

    public void add(Object Object, btRigidBody rigidBody, short marbleFlag, short allFlag) {
        rigidBody.setUserValue(instances.size);
        instances.add(Object);
        rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        dynamicsWorld.addRigidBody(rigidBody/*, PhysicsEngine.MARBLE_FLAG, PhysicsEngine.ALL_FLAG*/);
    }

    public void create(T gameEngine) {
        Bullet.init();
        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher      = new btCollisionDispatcher(collisionConfig);
        broadphase      = new btDbvtBroadphase();
//        collisionWorld  = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld    = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -10f, 0));

        dynamicsWorld.setDebugDrawer(debugDrawer);
//        btCollisionAlgorithmConstructionInfo ci = new btCollisionAlgorithmConstructionInfo();
//        ci.setDispatcher1(dispatcher);
//            algorithm = new btSphereBoxCollisionAlgorithm(null, ci, co0.wrapper, co1.wrapper, false);
    }

    public void dispose() {
        dynamicsWorld.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfig.dispose();
    }

    public void remove(GameObject<T> gameObject, btRigidBody rigidBody) {
        instances.removeValue(gameObject, false);
        dynamicsWorld.removeRigidBody(rigidBody);
    }

    public void render(MovingCamera camera) {
//        logger.info(String.format("testing %d objects collisions...", collisionWorld.getNumCollisionObjects()));
//        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
//        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);
//        logger.info("collision test start");
        dynamicsWorld.performDiscreteCollisionDetection();
//        logger.info("collision test end");
//        for (GameObject<T> obj : instances)
//            obj.body.getWorldTransform(obj.instance.transform);//TODO remove and ensure callback is working
        if (debug) {
            debugDrawer.begin(camera);
            dynamicsWorld.debugDrawWorld();
            debugDrawer.end();
        }
    }


}
