/*
* JavaTokenMarker.java - Java token marker
* Copyright (C) 1999 Slava Pestov
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
*/
package org.gjt.sp.jedit.syntax;

/**
* Java token marker.
*
* @author Slava Pestov
* @version $Id: JavaTokenMarker.java 2618 2004-06-17 14:03:32Z mik $
*/
public class JavaTokenMarker extends CTokenMarker
{
    public JavaTokenMarker()
    {
        super(getKeywords());
    }

    public static KeywordMap getKeywords()
    {
        if(javaKeywords == null) {
            javaKeywords = new KeywordMap(false);
            javaKeywords.add("abstract",Token.KEYWORD1);
            javaKeywords.add("final",Token.KEYWORD1);
            javaKeywords.add("private",Token.KEYWORD1);
            javaKeywords.add("protected",Token.KEYWORD1);
            javaKeywords.add("public",Token.KEYWORD1);
            javaKeywords.add("static",Token.KEYWORD1);
            javaKeywords.add("synchronized",Token.KEYWORD1);
            javaKeywords.add("volatile",Token.KEYWORD1);
            javaKeywords.add("transient",Token.KEYWORD1);
            javaKeywords.add("break",Token.KEYWORD1);
            javaKeywords.add("case",Token.KEYWORD1);
            javaKeywords.add("continue",Token.KEYWORD1);
            javaKeywords.add("default",Token.KEYWORD1);
            javaKeywords.add("do",Token.KEYWORD1);
            javaKeywords.add("else",Token.KEYWORD1);
            javaKeywords.add("for",Token.KEYWORD1);
            javaKeywords.add("if",Token.KEYWORD1);
            javaKeywords.add("instanceof",Token.KEYWORD1);
            javaKeywords.add("new",Token.KEYWORD1);
            javaKeywords.add("return",Token.KEYWORD1);
            javaKeywords.add("switch",Token.KEYWORD1);
            javaKeywords.add("while",Token.KEYWORD1);
            javaKeywords.add("throw",Token.KEYWORD1);
            javaKeywords.add("try",Token.KEYWORD1);
            javaKeywords.add("catch",Token.KEYWORD1);
            javaKeywords.add("finally",Token.KEYWORD1);
            javaKeywords.add("throws",Token.KEYWORD1);
            javaKeywords.add("assert",Token.KEYWORD1);

            javaKeywords.add("package",Token.KEYWORD2);
            javaKeywords.add("import",Token.KEYWORD2);
            javaKeywords.add("class",Token.KEYWORD2);
            javaKeywords.add("interface",Token.KEYWORD2);
            javaKeywords.add("extends",Token.KEYWORD2);
            javaKeywords.add("implements",Token.KEYWORD2);

            javaKeywords.add("this",Token.KEYWORD3);
            javaKeywords.add("null",Token.KEYWORD3);
            javaKeywords.add("super",Token.KEYWORD3);
            javaKeywords.add("true",Token.KEYWORD3);
            javaKeywords.add("false",Token.KEYWORD3);

            javaKeywords.add("byte",Token.PRIMITIVE);
            javaKeywords.add("char",Token.PRIMITIVE);
            javaKeywords.add("short",Token.PRIMITIVE);
            javaKeywords.add("int",Token.PRIMITIVE);
            javaKeywords.add("long",Token.PRIMITIVE);
            javaKeywords.add("float",Token.PRIMITIVE);
            javaKeywords.add("double",Token.PRIMITIVE);
            javaKeywords.add("boolean",Token.PRIMITIVE);
            javaKeywords.add("void",Token.PRIMITIVE);
            javaKeywords.add("enum",Token.PRIMITIVE);
        }
        return javaKeywords;
    }

    // private members
    private static KeywordMap javaKeywords;
}

/*
* ChangeLog:
* $Log$
* Revision 1.8  2004/06/17 14:03:32  mik
* next stage of text evaluator: does syntax colouring now,
can evaluate most expressions and statements
still work in progress
*
* Revision 1.7  2004/04/20 06:23:16  bquig
* Added enum as keyword to editor syntax recognition as partial support for "Tiger" (j2sdk 1.5)
*
* Revision 1.6  2003/04/10 13:47:48  fisker
* removed more unused imports
*
* Revision 1.5  2002/03/26 10:20:43  mik
* added "-source 1.4" option to javadoc call when appropriate
* added "assert" to syntax highlight set
*
* Revision 1.4  2000/01/14 06:33:16  mik
* fixed little font pref bug
*
* Revision 1.3  2000/01/14 04:35:18  mik
*
* changed colours again
*
* Revision 1.2  2000/01/14 03:33:04  mik
* changed syntax colours
*
* Revision 1.1  2000/01/12 03:17:59  bruce
*
* Addition of Syntax Colour Highlighting Package to CVS tree.  This is LGPL code used in the Moe Editor to provide syntax highlighting.
*
* Revision 1.3  1999/06/05 00:22:58  sp
* LGPL'd syntax package
*
* Revision 1.2  1999/04/22 06:03:26  sp
* Syntax colorizing change
*
* Revision 1.1  1999/03/13 09:11:46  sp
* Syntax code updates, code cleanups
*
*/
