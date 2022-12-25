# Setting up the KiWis Plugin

## Initialize the Repository ##

Create the GitHub public repository `owf-tstool-kiwis-plugin`.

Clone to the `TSTool/git-repos` folder on the computer.

Copy the `README.md`, `.gitignore`, `.gitattributes`, and `build-util/` files from the AWS plugin repository.
The `README.md` and `build-util/` files will be updated as the code is updated.

## Add Maven Project to Eclipse ##

Start the TSTool Eclipse environment by running the `cdss-app-tstool-main\build-util\run-eclipse-win64.cmd` command file
from a Windows command shell.

Use the Eclipse ***File / New / Project... / Maven / Maven Project***.  Specify information as shown in the following image.
Redundant `owf-tstool-kiwis-plugin` folders are used, one for the Git repository folder working files,
and one for the Maven project with source files.
This allows other top-level folders to be created in the repository to separate major development files, including documentation and tests.

![](new-maven-project1.png)

Fill out the new maven project artifact properties as follows:

![](new-maven-project2.png)

