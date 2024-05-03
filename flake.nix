{
  inputs = {
    nixpkgs = { url = "github:nixos/nixpkgs/nixpkgs-unstable"; };
    flake-utils = { url = "github:numtide/flake-utils"; };
  };
  outputs = { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        python = pkgs.python311;
        pythonPackages = python.pkgs;
        venvDir = "./env";
        lib-path = with pkgs;
          lib.makeLibraryPath [ libffi openssl stdenv.cc.cc python ];

      in {
        devShell = pkgs.mkShell {
          name = "basicPython";
          inherit venvDir;

          buildInputs = with pkgs;
            [
              # In this particular example, in order to compile any binary extensions they may
              # require, the Python modules listed in the hypothetical requirements.txt need
              # the following packages to be installed locally:
              taglib
              openssl
              git
              libxml2
              libxslt
              libzip
              zlib
            ] ++ (with pythonPackages; [
              # A Python interpreter including the 'venv' module is required to bootstrap
              # the environment.
              python

              # This executes some shell code to initialize a venv in $venvDir before
              # dropping into the shell
              venvShellHook

              # Those are dependencies that we would like to use from nixpkgs, which will
              # add them to PYTHONPATH and thus make them accessible from within the venv.
              numpy
              requests

              pygame
            ]);

          # Run this command, only after creating the virtual environment
          postVenvCreation = ''
            unset SOURCE_DATE_EPOCH
            pip install -r requirements.txt
          '';

          shellHook = ''
            export "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${lib-path}"
              if [ ! -d $venvDir ]; then
                echo "Creating virtual environment in $venvDir"
                ${pythonPackages.python}/bin/python -m venv $venvDir
              else
                echo "Using existing virtual environment in $venvDir"
              fi
              source $venvDir/bin/activate
          '';

          # Now we can execute any commands within the virtual environment.
          # This is optional and can be left out to run pip manually.
          postShellHook = ''
            # allow pip to install wheels
            unset SOURCE_DATE_EPOCH
          '';
        };
      });
}

