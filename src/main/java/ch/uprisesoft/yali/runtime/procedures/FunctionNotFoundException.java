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
package ch.uprisesoft.yali.runtime.procedures;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class FunctionNotFoundException  extends RuntimeException{
    
    private String name;
    
    public FunctionNotFoundException(String name) {
        super("Could not find function " + name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}