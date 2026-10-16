# Central image builds

The feature branch builds the reviewed Central source on Linux/amd64. Pushes to
`feat/central-opensearch-migration` publish to GHCR with the full commit tag
`sha-<40-character-commit>`. Pull requests and manual runs build without publishing
unless the manual publish input is selected. No workflow deploys to Kubernetes.

`images.json` lists build components and pinned external source contexts. Run
`python3 ci/central/check.py` before pushing. Use `python3 ci/central/build.py
--component NAME --plan` to inspect a recipe. Builds require Docker Buildx and set
OCI source/revision labels; NoOps verifies these labels and resolves image digests.
Private producer packages must grant the NoOps repository Actions read access.
The cluster pull identity also needs read access to every resulting package.

NoOps locks these exact source commits and assembles the operator package. Build
outputs and credentials are ignored; keep local operator bindings outside Git.
