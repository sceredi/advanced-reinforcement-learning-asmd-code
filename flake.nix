{
  inputs = {
    nixpkgs = { url = "github:nixos/nixpkgs/nixpkgs-unstable"; };
    flake-utils = { url = "github:numtide/flake-utils"; };
  };
  outputs = { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        pythonPackages = pkgs.python311Packages;
        venvDir = "./env";

      in {
        devShell = pkgs.mkShell {
          name = "basicPython";
          inherit venvDir;

          nativeBuildInputs = with pythonPackages; [ python ];
          shellHook = ''
            if [ ! -d $venvDir ]; then
              echo "Creating virtual environment in $venvDir"
              ${pythonPackages.python}/bin/python -m venv $venvDir
            else
              echo "Using existing virtual environment in $venvDir"
            fi
            source $venvDir/bin/activate
          '';
        };
      });
}

