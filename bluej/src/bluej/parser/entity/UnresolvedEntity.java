/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009  Michael Kolling and John Rosenberg 
 
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
package bluej.parser.entity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bluej.debugger.gentype.JavaType;

/**
 * Represents a java entity whose nature (value or type) is not yet known,
 * and provides a static method (getEntity) to obtain instances.
 * 
 * @author Davin McCall
 */
public class UnresolvedEntity extends JavaEntity
{
    private EntityResolver resolver;
    private List<String> names;
    private List<JavaEntity> typeArguments;
    private String querySource;
    
    /**
     * Get an entity whose type (value or class) is not yet known. The returned entity
     * can later be resolved to either a value or type.
     */
    public static JavaEntity getEntity(EntityResolver resolver, String name, String querySource)
    {
        return new UnresolvedEntity(resolver, name, querySource);
    }
    
    protected UnresolvedEntity(EntityResolver resolver, String name, String querySource)
    {
        this.resolver = resolver;
        this.names = new LinkedList<String>();
        names.add(name);
        this.querySource = querySource;
    }
    
    protected UnresolvedEntity(EntityResolver resolver, List<String> names,
            String querySource, List<JavaEntity> typeArguments)
    {
        this.resolver = resolver;
        this.names = names;
        this.typeArguments = typeArguments;
        this.querySource = querySource;
    }

    @Override
    public String getName()
    {
        return names.get(names.size() - 1);
    }

    @Override
    public JavaEntity getSubentity(String name)
    {
        List<String> newNames = new LinkedList<String>();
        newNames.addAll(names);
        newNames.add(name);
        return new UnresolvedEntity(resolver, newNames, querySource, typeArguments);
    }

    @Override
    public JavaType getType()
    {
        return null;
    }
    
    @Override
    public JavaEntity setTypeArgs(List<JavaEntity> tparams)
    {
        return new UnresolvedEntity(resolver, names, querySource, tparams);
    }

    @Override
    public JavaEntity resolveAsValue()
    {
        Iterator<String> i = names.iterator();
        String name = i.next();
        JavaEntity entity = resolver.getValueEntity(name, querySource);
        while (entity != null && i.hasNext()) {
            entity = entity.getSubentity(i.next());
        }
        if (entity != null) {
            return entity.resolveAsValue();
        }
        return null;
    }
    
    @Override
    public ClassEntity resolveAsType()
    {
        Iterator<String> i = names.iterator();
        PackageOrClass entity = resolver.resolvePackageOrClass(i.next(), querySource);
        while (entity != null && i.hasNext()) {
            entity = entity.getPackageOrClassMember(i.next());
        }
        if (entity != null) {
            return entity.resolveAsType();
        }
        return null;
    }
}
