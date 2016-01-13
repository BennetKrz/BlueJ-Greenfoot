/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009,2016  Michael Kolling and John Rosenberg 
 
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
package bluej.groupwork.git;

import bluej.groupwork.TeamworkCommandError;
import bluej.groupwork.TeamworkCommandResult;
import bluej.utility.Debug;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * A git command to commit all files.
 *
 * @author Fabio Hedayioglu
 */
public class GitCommitAllCommand extends GitCommand 
{

    protected Set<File> newFiles;
    protected Set<File> deletedFiles;
    protected String commitComment;

    public GitCommitAllCommand(GitRepository repository, Set<File> newFiles,
            Set<File> deletedFiles, String commitComment) 
    {
        super(repository);
        this.newFiles = newFiles;
        this.deletedFiles = deletedFiles;
        this.commitComment = commitComment;
    }

    @Override
    public TeamworkCommandResult getResult()
    {
        try (Git repo = Git.open(this.getRepository().getProjectPath())) {
            CommitCommand commit = repo.commit();
            //stage new files
            
            //jGit works with relative paths.
            Path basePath;
            basePath = Paths.get(this.getRepository().getProjectPath().toString());
            
            
            for (File f:newFiles){
                String fileName = basePath.relativize(f.toPath()).toString();
                if (!fileName.isEmpty()){
                    repo.add().addFilepattern(fileName).call();
                }
            }

            //deleted files are handled by the commit command.
            //by setting setAll to true, we are including modified
            //and deleted files to the commit.
            commit.setAll(true);
            //add the comment to the commit.
            commit.setMessage(commitComment);
            //set name and email of the author of the commit.
            commit.setAuthor(getRepository().getYourName(), getRepository().getYourEmail());
            commit.call();

        } catch (IOException | GitAPIException ex) {
            Debug.reportError(ex.getMessage());
            return new TeamworkCommandError(ex.getMessage(), ex.getLocalizedMessage());
        }

        return new TeamworkCommandResult();
    }

}