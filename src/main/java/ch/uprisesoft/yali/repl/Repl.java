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
package ch.uprisesoft.yali.repl;

import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.interpreter.InterpreterBuilder;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.scope.Scope;
import ch.uprisesoft.yali.scope.VariableNotFoundException;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Repl implements InputGenerator, OutputObserver {

    private final Interpreter interpreter;

    private final InputStreamReader input;
    private final PrintStream output;

    private boolean procDefinitionMode = false;
    private StringBuilder procDefinition;

    private ResourceBundle messages;

    public Repl(InputStreamReader input, PrintStream output) {
        this.input = input;
        this.output = output;

        messages = ResourceBundle.getBundle("Translation", Locale.getDefault());

        procDefinitionMode = false;

//        ProcedureDispatcher fd = new ProcedureDispatcher();
//        Scope rootScope = new Scope("root");
        interpreter = new InterpreterBuilder().build();
//        interpreter = new Interpreter(fd, rootScope);
        interpreter.loadStdLib(this, this);

    }

    public Repl() {
        this(new InputStreamReader(System.in), new PrintStream(System.out));
    }

    public void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(input);
        
        output.println("Welcome to Yali. To exit type bye and press <ENTER>");

        for (;;) {
            
            if (procDefinitionMode) {
                output.print(": ");
            } else {
                output.print("> ");
            }
            
            output.flush();
            
            String source = reader.readLine();
            
            if (source.toLowerCase().equals("bye")) {
                break;
            } else if (source.toLowerCase().startsWith("edit")) {
                
            }

            if (!procDefinitionMode && source.toLowerCase().startsWith("to")) {
                procDefinitionMode = true;
                procDefinition = new StringBuilder();
                procDefinition.append(source).append("\n");
            } else if (procDefinitionMode && source.toLowerCase().startsWith("end")) {
                procDefinition.append(source).append("\n");
                run(procDefinition.toString());
                procDefinitionMode = false;
            } else if (procDefinitionMode && !source.toLowerCase().startsWith("end")) {
                procDefinition.append(source).append("\n");
            } else {
                run(source);
            }

        }
    }

    private void run(String source) {
        try {
            Node result = interpreter.eval(source);
            output.println("; " + result.toString());
        } catch (NodeTypeException nte) {
            if (nte.getExpected().contains(NodeType.PROCCALL) && nte.getReceived().equals(NodeType.SYMBOL)) {
                output.println(
                        String.format(
                                "; " + messages.getString("function_not_found"),
                                nte.getNode().getToken().getLexeme(),
                                nte.getReceived()
                        )
                );
            } else if (nte.getExpected().contains(NodeType.PROCCALL)) {
                output.println(
                        String.format(
                                "; " + messages.getString("redundant_argument"),
                                nte.getNode().getToken().getLexeme(),
                                nte.getReceived()
                        )
                );
            } else {
                output.println(
                        String.format(
                                "; " + messages.getString("not_expected"),
                                nte.getNode().toString(),
                                nte.getExpected(),
                                nte.getReceived()
                        )
                );
            }
        } catch (VariableNotFoundException vnfe) {
            output.println(
                    String.format(
                            "; " + messages.getString("variable_not_found"),
                            vnfe.getName()
                    )
            );
        }
    }
    
    private String editProc(Procedure fun) throws IOException, InterruptedException {
        
        File file = File.createTempFile("temp", ".txt");
        file.deleteOnExit();

//        Files.write(file.toPath(), fun.);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Process process = new ProcessBuilder()
                    .command("notepad.exe", file.toPath().toAbsolutePath().toString())
                    .directory(file.toPath().getParent().toFile())
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
        } else {
            Desktop.getDesktop().edit(file);
        }

        return Files
                .lines(file.toPath(), StandardCharsets.UTF_8)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String request() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String requestLine() {
        BufferedReader reader = new BufferedReader(input);
        String result = "";
        try {
            result = reader.readLine();
        } catch (IOException ex) {
            output.println(ex);
        }

        return result;
    }

    @Override
    public void inform(String output) {
        this.output.print(output);
    }

}
