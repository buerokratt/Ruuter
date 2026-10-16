#!/usr/bin/env python3
"""Offline checks for publishable Central build inputs."""
import ast,json,re,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
images=json.loads((ROOT/'ci/central/images.json').read_text())
for key,spec in images.items():
 assert re.fullmatch('[A-Za-z][A-Za-z0-9-]*',key),key
 file=ROOT/spec['dockerfile'];assert file.is_file(),file
 text=file.read_text();assert re.search(r'(?im)^FROM .+ AS runtime$',text),file
 assert not re.search(r'(?im)^FROM (upstream-|fresh-central-)',text),file
 for val in spec.get('contexts',{}).values():assert re.fullmatch(r'https://github.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\.git#[a-f0-9]{40}',val),val
# Check only files introduced/modified by this branch; upstream public fixtures are not new credentials.
base=json.loads((ROOT/'ci/central/source.json').read_text())['upstreamCommit']
files=subprocess.check_output(['git','diff','--name-only','--diff-filter=ACMR',base,'HEAD'],cwd=ROOT,text=True).splitlines()
for name in files:
 p=ROOT/name
 assert not any(x in {'.git','node_modules','__pycache__','evidence','installation-state','.central-build','.central-release'} for x in p.relative_to(ROOT).parts),name
 assert not p.name.endswith(('.jks','.key','.p12','.kubeconfig','.tar','.tar.gz','.bundle')),name
 data=p.read_bytes()
 assert not re.search(rb'(?m)^-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',data),name
 if p.suffix=='.py':ast.parse(data)
 if p.suffix=='.json':json.loads(data)
print(json.dumps({'status':'PASS','components':len(images),'changedFilesChecked':len(files),'clusterAccess':False}))
