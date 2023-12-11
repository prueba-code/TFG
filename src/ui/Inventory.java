package ui;

import listener.MouseListener;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import ui.widget.*;
import utils.render.Shader;
import utils.render.Window;
import utils.render.mesh.Mesh;
import utils.render.texture.Textures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representa un inventario y se encarga de registrar los clicks y dibujar el inventario en pantalla.
 */
public class Inventory {
    public static final int WIDGET_SPACING = 4;


    /**
     * Dimensiones del inventario, en unidades in-game, no píxeles de pantalla.
     */
    private float width, height;

    /**
     * Posición del inventario en el eje Y. Se cuenta de arriba a abajo.
     */
    private float posY;

    private float currentScrollPosX = 0;

    /**
     * Píxeles de patalla que ocupa un pixel de la interfaz in-game.
     */
    private float pixelSizeInScreen;

    private boolean isShowingLeftArrow, isShowingRightArrow;
    private int clickTimeLeftArrow = -1, clickTimeRightArrow = -1;
    private boolean isHoveredLeftArrow, isHoveredRightArrow;
    private int tabCount = 0;
    private TabWidget currentTab = null;

    private final Map<TabWidget, List<Widget>> WIDGETS_PER_TAB = new HashMap<>();

    private final List<TabWidget> TABS = new LinkedList<>();
    private List<Widget> currentWidgets = new LinkedList<>();

    public Inventory() {
        this.updateShowArrows();
    }


    public void addWidget(Widget widget, TabWidget tab) {
        if (!widget.getClass().isAnnotationPresent(IgnoreScrollMovement.class) && widget.getBoundingBox().getComponents().z() +4 > this.width) {
            this.width = widget.getBoundingBox().getComponents().z() +4;
        }

        if (tab == null) {
            if (widget instanceof TabWidget tabWidget) {
                this.tabCount++;
                this.TABS.add(tabWidget);
            }
        } else {
            List<Widget> widgetsInTab = this.WIDGETS_PER_TAB.getOrDefault(tab, new LinkedList<>());
            widgetsInTab.add(widget);
            this.WIDGETS_PER_TAB.put(tab, widgetsInTab);
        }
        if (widget instanceof RelocatableWhenResizedScreen) {
            this.onResizeWindowEvent(Window.getWidth(), Window.getHeight());
        }
    }

    public void addTab(TabWidget tab) {
        this.addWidget(tab, null);
    }

    public void removeWidget(Widget widget, TabWidget tab) {
        List<Widget> widgetsInTab = this.WIDGETS_PER_TAB.getOrDefault(tab, new LinkedList<>());
        widgetsInTab.remove(widget);
        this.WIDGETS_PER_TAB.put(tab, widgetsInTab);
    }

    public void removeWidget(Widget widget) {
        this.TABS.forEach(tab -> this.removeWidget(widget, tab));
    }

    /**
     * Dibuja el inventario según el <code>mesh</code> que se le pase. Siempre utiliza el shader <code>HUD</code>.
     * @param mesh <code>Mesh</code> que se va a utilizar para dibujar el inventario.
     */
    public void draw(Mesh mesh) {
        Shader.HUD.upload2f("uHudPosition", 0, this.posY);
        Shader.HUD.upload2f("uHudSize", Window.getWidth(), this.height);

        Textures.CONTAINER.bind();
        ARBVertexArrayObject.glBindVertexArray(mesh.getVaoId());
        GL20.glEnableVertexAttribArray(0);
        GL20.glDrawElements(GL20.GL_TRIANGLES, mesh.getElementArray().length, GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        ARBVertexArrayObject.glBindVertexArray(0);


        this.currentWidgets.forEach(widget -> {
            if (widget.getClass().isAnnotationPresent(IgnoreScrollMovement.class)) {
                Shader.HUD.upload2f("uHudPosition", this.pixelSizeInScreen * widget.getPosX(), this.pixelSizeInScreen * widget.getPosY() + this.posY);
                Shader.HUD.upload2f("uHudSize", this.pixelSizeInScreen * widget.getBoundingBox().getWidth(), this.pixelSizeInScreen * widget.getBoundingBox().getHeight());
            } else {
                Shader.HUD.upload2f("uHudPosition", this.pixelSizeInScreen * (widget.getPosX() + this.currentScrollPosX), this.pixelSizeInScreen * widget.getPosY() + this.posY);
                Shader.HUD.upload2f("uHudSize", this.pixelSizeInScreen * widget.getBoundingBox().getWidth(), this.pixelSizeInScreen * widget.getBoundingBox().getHeight());
            }

            widget.getTexture().bind();
            ARBVertexArrayObject.glBindVertexArray(mesh.getVaoId());
            GL20.glEnableVertexAttribArray(0);
            GL20.glDrawElements(GL20.GL_TRIANGLES, mesh.getElementArray().length, GL11.GL_UNSIGNED_INT, 0);
            GL20.glDisableVertexAttribArray(0);
            ARBVertexArrayObject.glBindVertexArray(0);

            if (widget instanceof CustomDrawWidget customDrawWidget) {
                if (widget.getClass().isAnnotationPresent(IgnoreScrollMovement.class)) {
                    customDrawWidget.draw(mesh, this.pixelSizeInScreen, this.pixelSizeInScreen * widget.getPosX(), this.pixelSizeInScreen * widget.getPosY() + this.posY, this.pixelSizeInScreen * widget.getBoundingBox().getWidth(), this.pixelSizeInScreen * widget.getBoundingBox().getHeight());
                } else {
                    customDrawWidget.draw(mesh, this.pixelSizeInScreen, this.pixelSizeInScreen * (widget.getPosX() + this.currentScrollPosX), this.pixelSizeInScreen * widget.getPosY() + this.posY, this.pixelSizeInScreen * widget.getBoundingBox().getWidth(), this.pixelSizeInScreen * widget.getBoundingBox().getHeight());
                }
            }
        });

        if (this.isShowingLeftArrow) {
            Shader.HUD.upload2f("uHudPosition", 0, this.posY);
            Shader.HUD.upload2f("uHudSize", this.pixelSizeInScreen * 7, this.height);

            (this.clickTimeLeftArrow>-1? Textures.SELECTED_LEFT_ARROW: (this.isHoveredLeftArrow? Textures.HOVERED_LEFT_ARROW: Textures.UNSELECTED_LEFT_ARROW)).bind();
            ARBVertexArrayObject.glBindVertexArray(mesh.getVaoId());
            GL20.glEnableVertexAttribArray(0);
            GL20.glDrawElements(GL20.GL_TRIANGLES, mesh.getElementArray().length, GL11.GL_UNSIGNED_INT, 0);
            GL20.glDisableVertexAttribArray(0);
            ARBVertexArrayObject.glBindVertexArray(0);

            if (this.clickTimeLeftArrow > -1) {
                this.clickTimeLeftArrow--;
            }
        }

        if (this.isShowingRightArrow) {
            Shader.HUD.upload2f("uHudPosition", Window.getWidth() - this.pixelSizeInScreen *7, this.posY);
            Shader.HUD.upload2f("uHudSize", this.pixelSizeInScreen * 7, this.height);

            (this.clickTimeRightArrow>-1?Textures.SELECTED_RIGHT_ARROW:  (this.isHoveredRightArrow? Textures.HOVERED_RIGHT_ARROW: Textures.UNSELECTED_RIGHT_ARROW)).bind();
            ARBVertexArrayObject.glBindVertexArray(mesh.getVaoId());
            GL20.glEnableVertexAttribArray(0);
            GL20.glDrawElements(GL20.GL_TRIANGLES, mesh.getElementArray().length, GL11.GL_UNSIGNED_INT, 0);
            GL20.glDisableVertexAttribArray(0);
            ARBVertexArrayObject.glBindVertexArray(0);
            
            if (this.clickTimeRightArrow > -1) {
                this.clickTimeRightArrow--;
            }
        }
    }

    /**
     * Establece cuantos píxeles de pantalla equivales a un pixel de la interfaz in-game. Actualiza las dimensiones de
     * la interfaz y su posición en el eje Y.
     * @param pixelSizeInScreen Equivalencia entre píxeles in-game y píxeles de pantalla.
     */
    public void setPixelSizeInScreen(float pixelSizeInScreen) {
        this.pixelSizeInScreen = pixelSizeInScreen;
        this.height = 41 *this.pixelSizeInScreen;
        this.posY = Window.getHeight() -(41 *this.pixelSizeInScreen);
        this.updateShowArrows();
    }

    public void onMouseMoveEvent(float mouseX, float mouseY) {
        float interfaceX = (mouseX / this.pixelSizeInScreen) - this.currentScrollPosX;
        float interfaceY = (mouseY - this.posY) / this.pixelSizeInScreen;

        AtomicBoolean hoverUnderArrows = new AtomicBoolean(true);

        if (this.isShowingLeftArrow) {
            this.isHoveredLeftArrow = interfaceX + this.currentScrollPosX <= 7 &&
                    interfaceY <= this.height;

            if (this.isHoveredLeftArrow) {
                    hoverUnderArrows.set(false);
            }
        }

        if (this.isShowingRightArrow) {
            this.isHoveredRightArrow = interfaceX +this.currentScrollPosX >= Window.getWidth() /this.pixelSizeInScreen -7 &&
                    interfaceY <= this.height;

            if (this.isHoveredRightArrow) {
                hoverUnderArrows.set(false);
            }
        }

        this.currentWidgets.forEach(widget -> {
            boolean isMouseInWidget;
            if (widget.getClass().isAnnotationPresent(IgnoreScrollMovement.class)) {
                isMouseInWidget = widget.getBoundingBox().containsLocation((mouseX / this.pixelSizeInScreen), interfaceY);
            } else {
                isMouseInWidget = widget.getBoundingBox().containsLocation(interfaceX, interfaceY);
            }

            if (isMouseInWidget) {
                widget.setHovered(hoverUnderArrows.get());
            } else {
                widget.setHovered(false);
            }
        });
    }

    public void onHoverEvent() {
        if (MouseListener.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            if (this.isHoveredLeftArrow) {
                this.moveSlider(.7f);
            } else if (this.isHoveredRightArrow) {
                this.moveSlider(-.7f);
            }
        }


        this.currentWidgets.stream()
                .filter(Widget::isHovered)
                .forEach(Widget::onHoverEvent);
    }

    /**
     * Evento que se llama cuando se hace clic en la interfaz y se lo pasa el Widget que se haya pulsado, siempre que
     * implemente la interfaz <code>ClickableWidget</code>
     * @param mouseX Posición en el eje X del ratón, en píxeles de pantalla.
     * @param mouseY Posición en el eje Y del ratón, en píxeles de pantalla.
     */
    public void onClickEvent(float mouseX, float mouseY) {
        new LinkedList<>(this.currentWidgets).stream()
                .filter(Widget::isHovered)
                .forEach(Widget::onClickEvent);
    }

    public void onResizeWindowEvent(float newWidth, float newHeight) {
        float tabOrigin = ((newWidth /this.pixelSizeInScreen) /(float) 2) - ((this.tabCount *TabWidget.TOTAL_TAB_SIZE) /(float) 2);

        TabWidget tabWidget;
        for (int tabIndex = 0; tabIndex < this.TABS.size(); tabIndex++) {
            tabWidget = this.TABS.get(tabIndex);

            tabWidget.setPosY(TabWidget.WIDGET_OFFSET_Y);
            tabWidget.setPosX(tabOrigin);
            tabOrigin += TabWidget.TOTAL_TAB_SIZE;
        }

        this.currentWidgets.stream()
                .filter(widget -> widget instanceof RelocatableWhenResizedScreen)
                .forEach(widget -> ((RelocatableWhenResizedScreen) widget).onResizeWindowEvent(newWidth /this.pixelSizeInScreen, newHeight /this.pixelSizeInScreen));
    }

    public boolean moveSlider(float amount) {
        float originalSliderPos = this.currentScrollPosX;

        this.currentScrollPosX += amount;
        if (this.currentScrollPosX > this.getMaxLeftSliderPos() || this.currentScrollPosX <= this.getMaxRightSliderPos()) {
            this.currentScrollPosX = originalSliderPos;
            return false;
        }

        if (amount > 0) {
            this.clickTimeLeftArrow = 10;
            this.clickTimeRightArrow = -1;
        } else {
            this.clickTimeLeftArrow = -1;
            this.clickTimeRightArrow = 10;
        }

        this.updateShowArrows();
        return true;
    }

    private double getMaxLeftSliderPos() {
        return 0;
    }

    private double getMaxRightSliderPos() {
        return -(this.width -(Window.getWidth() /this.pixelSizeInScreen));
    }

    public void updateShowArrows() {
        this.isShowingLeftArrow = this.currentScrollPosX - this.getMaxLeftSliderPos() < -1;
        this.isShowingRightArrow = this.getMaxRightSliderPos() - this.currentScrollPosX < -1;
    }

    public void unselectAllTabs() {
        this.TABS.forEach(TabWidget::unselect);
    }

    public void setCurrentTab(TabWidget currentTab) {
        this.currentTab = currentTab;
        
        this.currentWidgets = this.WIDGETS_PER_TAB.getOrDefault(this.currentTab, new LinkedList<>());
        this.currentWidgets.addAll(this.TABS);
    }
}
