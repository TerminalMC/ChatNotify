/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
