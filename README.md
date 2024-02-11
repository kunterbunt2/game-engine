# game-engine

Engine based on libgdx and gltf

# usage instructions

1. Create youro GameEngine class.<br>
   This class will do all the game specific part.<br>
   It needs to implement ApplicationListener interface.<br>
2. Create RenderEngine<GameEngine> field in your GanmeEngine class.
   This will do all the generic stuff that you can reuse in all your games.<br>
   The renderEnigne needs a font and a texture region to draw the CPU/GPU performance graph.<br>
   The renderEngine alo needs a camera that you define.<br>
3. Use RendreEngine methods add(), addDynamic(), addStatic() to add GameObjects into your scene.
3. In the render method called by libgdx, you need to call all the generic part.

---
renderEngine.cpuGraph.begin();
renderEngine.updateCamera(centerXD, centerYD, centerZD);
renderEngine.cpuGraph.end();
renderEngine.gpuGraph.begin();
renderEngine.render(currentTime, deltaTime, takeScreenShot);
renderEngine.gpuGraph.end();
renderEngine.handleQueuedScreenshot(takeScreenShot);
---

You can add any other rendering code you need inbetween.
