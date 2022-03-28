package me.rhin.openciv.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.events.type.BottomShapeRenderEvent;
import me.rhin.openciv.events.type.MouseHoveredEvent;
import me.rhin.openciv.events.type.ResizeEvent;
import me.rhin.openciv.events.type.ScrollEvent;
import me.rhin.openciv.events.type.TopShapeRenderEvent;
import me.rhin.openciv.shared.logging.Logger;
import me.rhin.openciv.shared.logging.LoggerFactory;
import me.rhin.openciv.shared.logging.LoggerType;
import me.rhin.openciv.ui.window.WindowManager;

public abstract class AbstractScreen implements Screen, InputProcessor {
	private static final Logger LOGGER = LoggerFactory.getInstance(LoggerType.LOG_TAG);

	protected WindowManager windowManager;
	protected OrthographicCamera camera;
	protected OrthographicCamera overlayCamera;
	protected float camX;
	protected float camY;
	protected StretchViewport viewport;
	private StretchViewport overlayViewport;
	protected Stage stage;
	protected Stage overlayStage;
	private InputMultiplexer inputMultiplexer;
	private ShapeRenderer bottomShapeRenderer;
	protected ShapeRenderer topShapeRenderer;
	private long movedMouseTime;
	private boolean mouseHovered;

	protected AbstractScreen() {
		this.windowManager = new WindowManager();
		this.camera = new OrthographicCamera();
		this.overlayCamera = new OrthographicCamera();
		// FIXME: Set a global var for width & height for game.
		this.camX = Gdx.graphics.getWidth() / 2;
		this.camY = Gdx.graphics.getHeight() / 2;
		this.viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
		this.overlayViewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), overlayCamera);

		this.stage = new Stage(viewport);
		this.overlayStage = new Stage(overlayViewport);

		this.bottomShapeRenderer = new ShapeRenderer();
		BottomShapeRenderEvent.setShapeRenderer(bottomShapeRenderer);

		this.topShapeRenderer = new ShapeRenderer();
		TopShapeRenderEvent.setShapeRenderer(topShapeRenderer);
	}

	public abstract ScreenEnum getType();

	@Override
	public void show() {
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(overlayStage);
		inputMultiplexer.addProcessor(stage);
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);

		// Re-implement listener for Soundhandler
		// FIXME: This is a stopgap measure since we clear all listeners every screen
		// change
		Civilization.getInstance().getEventManager().addListener(Civilization.getInstance().getSoundHandler());

		Civilization.getInstance().getEventManager().addListener(Civilization.getInstance().getChatHandler());

		this.movedMouseTime = System.currentTimeMillis();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.253F, 0.304F, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// camera.position.x = camX;
		// camera.position.y = camY;
		// camera.update();

		// overlayCamera.update();

		// Bottom stage
		stage.act();
		stage.draw();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		bottomShapeRenderer.setProjectionMatrix(camera.combined);
		bottomShapeRenderer.begin(ShapeType.Line);
		Civilization.getInstance().getEventManager().fireEvent(BottomShapeRenderEvent.INSTANCE);
		bottomShapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		// Middle Stage

		// Top Stage

		overlayStage.act();
		overlayStage.draw();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		topShapeRenderer.setProjectionMatrix(overlayCamera.combined);
		topShapeRenderer.begin(ShapeType.Line);
		Civilization.getInstance().getEventManager().fireEvent(TopShapeRenderEvent.INSTANCE);

		if (Civilization.DEBUG_BOUNDING_BOXES) {
			for (Actor actor : overlayStage.getActors()) {

				topShapeRenderer.setColor(Color.RED);

				// Bottom square
				topShapeRenderer.line(actor.getX(), actor.getY() + 1, actor.getX() + actor.getWidth(),
						actor.getY() + 1);
				// Top square
				topShapeRenderer.line(actor.getX(), actor.getY() + actor.getHeight(), actor.getX() + actor.getWidth(),
						actor.getY() + actor.getHeight());

				// Left square
				topShapeRenderer.line(actor.getX() + 1, actor.getY(), actor.getX() + 1,
						actor.getY() + actor.getHeight());
				// Right square
				topShapeRenderer.line(actor.getX() + actor.getWidth(), actor.getY(), actor.getX() + actor.getWidth(),
						actor.getY() + actor.getHeight());
			}
		}
		topShapeRenderer.setColor(Color.WHITE);

		topShapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		if (Civilization.DEBUG_GL) {
			LOGGER.info("  Drawcalls: " + Civilization.getInstance().getProfiler().getDrawCalls() + ", Calls: "
					+ Civilization.getInstance().getProfiler().getCalls() + ", TextureBindings: "
					+ Civilization.getInstance().getProfiler().getTextureBindings() + ", ShaderSwitches: "
					+ Civilization.getInstance().getProfiler().getShaderSwitches() + ", VertexCount: "
					+ Civilization.getInstance().getProfiler().getVertexCount().value);
			Civilization.getInstance().getProfiler().reset();
		}

		if (System.currentTimeMillis() - movedMouseTime >= 1000 && !mouseHovered) {
			float mouseX = Gdx.input.getX();
			float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
			mouseHovered = true;

			Civilization.getInstance().getEventManager().fireEvent(new MouseHoveredEvent(mouseX, mouseY));
		}
	}

	@Override
	public void resize(int width, int height) {
		Civilization.getInstance().getEventManager().fireEvent(new ResizeEvent(width, height));

		viewport.setWorldSize(width, height);
		viewport.update(width, height, true);

		overlayViewport.setWorldSize(width, height);
		overlayViewport.update(width, height, true);

		camera.update();
		overlayCamera.update();
	}

	@Override
	public void dispose() {
		stage.dispose();
		overlayStage.dispose();
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public boolean keyDown(int keycode) {
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		movedMouseTime = System.currentTimeMillis();
		mouseHovered = false;
		return true;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		Civilization.getInstance().getEventManager().fireEvent(new ScrollEvent(amountX, amountY));
		return true;
	}

	public void setCameraPosition(float camX, float camY) {
		this.camX = camX;
		this.camY = camY;
	}

	public void translateCamera(float x, float y, float z) {
		camX += x;
		camY += y;
	}

	public Viewport getViewport() {
		return viewport;
	}

	public Stage getStage() {
		return stage;
	}

	public Stage getOverlayStage() {
		return overlayStage;
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public InputMultiplexer getInputMultiplexer() {
		return inputMultiplexer;
	}

	public WindowManager getWindowManager() {
		return windowManager;
	}
}
