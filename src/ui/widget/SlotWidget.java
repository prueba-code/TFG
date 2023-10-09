package ui.widget;

import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import utils.render.Shader;
import utils.render.mesh.Mesh;
import utils.render.texture.StaticTexture;
import utils.render.texture.Texture;

public class SlotWidget extends Widget implements CustomDrawWidget {
    private final Texture CONTENT;

    public SlotWidget(float posX, float posY, Texture content) {
        super(posX, posY);
        this.CONTENT = content;
    }

    @Override
    public float getHeight() {
        return 16;
    }

    @Override
    public float getWidth() {
        return 16;
    }

    @Override
    public Texture getTexture() {
        return new StaticTexture("assets/textures/ui/inventory/unselected_slot.png");
    }

    @Override
    public void draw(Mesh mesh, float pixelSizeInScreen, float posX, float posY, float width, float height) {
        Shader.HUD.upload2f("hudPosition", posX + 2.5f * pixelSizeInScreen, posY + 2.5f * pixelSizeInScreen);
        Shader.HUD.upload2f("hudSize", (this.getWidth() -5) * pixelSizeInScreen, (this.getHeight() -5) * pixelSizeInScreen);

        this.CONTENT.bind();
        ARBVertexArrayObject.glBindVertexArray(mesh.getVaoId());
        GL20.glEnableVertexAttribArray(0);
        GL20.glDrawElements(GL20.GL_TRIANGLES, mesh.getElementArray().length, GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        ARBVertexArrayObject.glBindVertexArray(0);
        this.CONTENT.bind();
    }
}
