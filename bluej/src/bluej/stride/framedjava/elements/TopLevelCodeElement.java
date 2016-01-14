/*
 This file is part of the BlueJ program. 
 Copyright (C) 2014,2015,2016 Michael Kölling and John Rosenberg
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package bluej.stride.framedjava.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

import bluej.debugger.gentype.ConstructorReflective;
import bluej.stride.framedjava.ast.FrameFragment;
import bluej.stride.framedjava.ast.TypeSlotFragment;
import bluej.stride.generic.AssistContentThreadSafe;
import bluej.stride.generic.InteractionManager;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import threadchecker.OnThread;
import threadchecker.Tag;
import bluej.parser.CodeSuggestions;
import bluej.parser.entity.EntityResolver;
import bluej.stride.framedjava.ast.JavaFragment.PosInSourceDoc;
import bluej.stride.framedjava.ast.JavaSource;
import bluej.stride.framedjava.ast.Loader;
import bluej.stride.framedjava.errors.CodeError;
import bluej.stride.framedjava.errors.SyntaxCodeError;
import bluej.stride.framedjava.frames.TopLevelFrame;
import bluej.stride.framedjava.slots.ExpressionSlot;

public interface TopLevelCodeElement
{
    // This is primarily a marker for ClassElement and InterfaceElement,
    // to try to make sure we don't pass the wrong element when a top-level element is intended
    
    // Helper methods:
    public static LinkedList<String> xmlToStringList(Element el, String container, String itemName, String itemAttribute)
    {
        LinkedList<String> members = new LinkedList<>();
        Element collectionChildElement = el.getFirstChildElement(container);
        if (collectionChildElement != null ) {
            Elements children = collectionChildElement.getChildElements();
            for (int i = 0; i < children.size(); i++) {
                final Element child = children.get(i);
                if (child.getLocalName().equals(itemName)) {
                    members.add(child.getAttributeValue(itemAttribute));
                }
                else {
                    bluej.utility.Debug.reportError("Wrong element format: expected '" + itemName + "', found '" + child.getLocalName() + "'.");
                }
            }
        }
        return members;
    }

    // Makes an XML element named container, with an element per string (of type itemName) with content put in the given itemAttribute
    public static Element stringListToXML(List<TypeSlotFragment> items, String container, String itemName, String itemAttribute)
    {
        Element el = new Element(container);
        for (TypeSlotFragment s : items)
        {
            LocatableElement child = new LocatableElement(null, itemName);
            child.addAttributeCode(itemAttribute, s);
            el.appendChild(child);
        }
        return el;
    }

    public static List<CodeElement> fillChildrenElements(ContainerCodeElement parent, Element el, String string)
    {
        List<CodeElement> members = new ArrayList<CodeElement>();
        Element collectionChildElement = el.getFirstChildElement(string);
        if (collectionChildElement != null ) {
            Elements children = collectionChildElement.getChildElements();
            for (int i = 0; i < children.size(); i++) {
                CodeElement member = Loader.loadElement(children.get(i));
                members.add(member);
                member.setParent(parent);
            }
        }
        return members;
    }

    public static Attribute getStrideVersionAttribute()
    {
        return new Attribute("strideversion", "1");
    }

    @OnThread(Tag.Swing)
    public CodeSuggestions getCodeSuggestions(PosInSourceDoc pos, ExpressionSlot<?> completing);

    @OnThread(Tag.FX)
    public TopLevelFrame<? extends TopLevelCodeElement> createTopLevelFrame(InteractionManager editor);

    public List<ImportElement> getImports();

    public String getName();
    
    // Used to help style the tab:
    public String getStylePrefix();

    @OnThread(Tag.Swing)
    public EntityResolver getResolver();

    public @OnThread(Tag.FX) InteractionManager getEditor();

    // Methods mirroring CodeElement:
    public Element toXML();

    public TopLevelFrame getFrame();

    public Stream<CodeElement> streamContained();

    public Stream<SyntaxCodeError> findEarlyErrors();

    public Stream<Future<List<CodeError>>> findDirectLateErrors(InteractionManager editor);
    
    public JavaSource toJavaSource();

    public default JavaSource toJavaSource(boolean warning)
    {
        JavaSource java = toJavaSource();
        if (warning) {
            // TODO AA make it non-compiled fragment

            // Clone before modifying:
            java = new JavaSource(java);
            java.prependLine(Arrays.asList(new FrameFragment(null, "// WARNING: This file is auto-generated and any changes to it will be overwritten")), null);
        }
        return java;
    }

    void updateSourcePositions();

    @OnThread(Tag.Swing)
    List<ConstructorReflective> getSuperConstructors();

    @OnThread(Tag.FX)
    List<AssistContentThreadSafe> getThisConstructors();
}
