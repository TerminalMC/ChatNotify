package dev.terminalmc.chatnotify.gui.widget.field;

public class FakeTextField extends TextField {
    private final Runnable onClick;

    public FakeTextField(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
        this.active = false;
        this.setResponder((str) -> {});
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        return (visible
                && mouseX >= (double)getX()
                && mouseY >= (double)getY()
                && mouseX < (double)(getX() + getWidth())
                && mouseY < (double)(getY() + getHeight()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clicked(mouseX, mouseY)) {
            onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.run();
    }
}
