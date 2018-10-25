/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.fsm.utilities;

import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

/**
 * The generator creates graphical graph representations of a finite state machine in the graphical
 * language of <a href="http://www.graphviz.org/">Graphviz dot</a> language.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public class GeneratorGraphDot<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    private static final int MAX_NR_STATES = 1000;

    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorGraphDot(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        writePreamble(writer);
        states.stream().sorted().forEach(state -> writeState(state, 1, writer));
        states.stream().sorted().forEach(state -> writeTransitions(state, writer));
        writePostamble(writer);
        writer.flush();
        writer.close();
    }

    @Override
    public String extension() {
        return ("dot");
    }


    private void writePreamble(@NotNull PrintWriter writer) {
        writer.append("digraph G {").println();
        indent(writer, INDENTATION);
        writer.append("compound=true;\n").println();
    }

    private void writePostamble(@NotNull PrintWriter writer) {
        writer.append("}").println();
    }

    private void writeState(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        int spaces = INDENTATION * depth;
        indent(writer, spaces);
        if (state.isComposite()) {
            writer.append("subgraph cluster").append(Integer.toString(MAX_NR_STATES + state.getId().ordinal())).println(" {");
            indent(writer, spaces + INDENTATION).println("style=invis;");
            spaces += INDENTATION;
            indent(writer, spaces).append("subgraph cluster").append(getStateId(state)).println(" {");
            spaces += INDENTATION;
            indent(writer, spaces).append(getStyle(state)).println(";");
            indent(writer, spaces).append("label = \"").append(state.getId().name()).println("\"");
            state.substates().stream().sorted().forEach(o -> writeState(o, depth + 2, writer));
            spaces -= INDENTATION;
            indent(writer, spaces).println("}");
            spaces -= INDENTATION;
            indent(writer, spaces).println("}");
        } else {
            writer.append(state.getId().name()).append(" [").append(getStyle(state)).println("];");
        }
    }

    private void writeTransitions(@NotNull State<O, S, E> state, @NotNull PrintWriter writer) {
        state.transitions().stream().sorted(transitionComparator()).forEach(transition -> {
            var source = transition.source();
            var target = transition.target();
            indent(writer, INDENTATION).append(getStateName(findAnyNonCompositeSubstate(source))).append(" -> ")
                    .append(getStateName(findAnyNonCompositeSubstate(target))).append(" [");
            if (source.isComposite()) {
                writer.append("ltail=cluster").append(getStateId(source)).append(", ");
            }
            if (target.isComposite()) {
                writer.append("lhead=cluster").append(getStateId(target)).append(", ");
            }
            writer.append("label=\"").append(transition.eventId().name());
            if (transition.hasGuard()) {
                writer.append("[").append(transition.guardDescription() != null ? transition.guardDescription() : "").append("]");
            }
            if (transition.hasAction()) {
                writer.append("/").append(transition.actionDescription() != null ? transition.actionDescription() : "");
            }
            writer.println("\"];");
        });
    }


    private @NotNull State<O, S, E> findAnyNonCompositeSubstate(@NotNull State<O, S, E> state) {
        return state.isComposite() ? findAnyNonCompositeSubstate(state.substates().iterator().next()) : state;
    }

    private String getStyle(@NotNull State<O, S, E> state) {
        StringBuilder buffer = new StringBuilder();
        String separator = "";
        buffer.append("style=\"");
        if (state.isComposite()) {
            buffer.append(separator).append("visible");
            separator = ", ";
        }
        if (state.isInitial()) {
            buffer.append(separator).append("dashed");
            separator = ", ";
        }
        if (state.isFinal()) {
            buffer.append(separator).append("bold");
            separator = ", ";
        }
        if (state.hasHistory()) {
            buffer.append(separator).append("filled");
            separator = ", ";
        }
        buffer.append("\"");
        return (separator.equals(", ") ? buffer.toString() : "");
    }
}
