/* 
 * Copyright 2020 Uprise Software.
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
package ch.uprisesoft.yali.runtime.procedures.builtin;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class MockTurtle {
    List<TurtlePosition> positions;

    public MockTurtle() {
        positions = new ArrayList<>();
        positions.add(new TurtlePosition(0, 0, 0));
    }
    
    public List<TurtlePosition> getPositions() {
        return positions;
    }
    
    public TurtlePosition getPosition() {
        return positions.get(positions.size() -1);
    }
    
    public void fd(int steps) {
        TurtlePosition oldpos = positions.get(positions.size() - 1);
        float angle = oldpos.angle;
        float oldx = oldpos.x;
        float oldy = oldpos.y;
        float newy = oldy + (float) (steps * Math.cos(Math.toRadians(angle)));
        float newx = oldx + (float) (steps * Math.sin(Math.toRadians(angle)));
        positions.add(new TurtlePosition(newx, newy, angle));
    }

    public void bk(int steps) {
        TurtlePosition oldpos = positions.get(positions.size() - 1);
        float angle = oldpos.angle;
        float oldx = oldpos.x;
        float oldy = oldpos.y;
        float newy = oldy - (float) (steps * Math.cos(Math.toRadians(angle)));
        float newx = oldx - (float) (steps * Math.sin(Math.toRadians(angle)));
        positions.add(new TurtlePosition(newx, newy, angle));
    }

    public void lt(float degrees) {
        TurtlePosition oldpos = positions.get(positions.size() - 1);
        positions.set(positions.size() - 1, new TurtlePosition(oldpos.x, oldpos.y, oldpos.angle - degrees));
    }

    public void rt(int degrees) {
        TurtlePosition oldpos = positions.get(positions.size() - 1);
        positions.set(positions.size() - 1, new TurtlePosition(oldpos.x, oldpos.y, oldpos.angle + degrees));
    }
    
    public void cs() {
        positions.clear();
        positions.add(new TurtlePosition(0, 0, 0));
    }
}
