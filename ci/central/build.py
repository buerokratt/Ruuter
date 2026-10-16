#!/usr/bin/env python3
"""Build one named image from this repository; publication is explicit."""
import argparse,json,os,re,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
def run(args):subprocess.run(args,cwd=ROOT,check=True)
def main():
 p=argparse.ArgumentParser();p.add_argument('--component',required=True);p.add_argument('--publish',action='store_true');p.add_argument('--plan',action='store_true');a=p.parse_args()
 catalog=json.loads((ROOT/'ci/central/images.json').read_text());spec=catalog[a.component]
 repo=os.environ.get('GITHUB_REPOSITORY','').lower();sha=os.environ.get('GITHUB_SHA','')
 if not re.fullmatch(r'[a-z0-9_.-]+/[a-z0-9_.-]+',repo) or not re.fullmatch('[a-f0-9]{40}',sha):raise ValueError('explicit_repository_and_commit_required')
 tag='ghcr.io/'+repo+'/central-'+a.component.lower()+':sha-'+sha
 contexts=[]
 for key,value in spec.get('contexts',{}).items():
  if not re.fullmatch(r'https://github.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\.git#[a-f0-9]{40}',value):raise ValueError('immutable_public_source_context_required')
  contexts+=['--build-context',key+'='+value]
 common=['docker','buildx','build','--platform','linux/amd64','--file',spec['dockerfile'],*contexts]
 tests=common+['--target',spec['testTarget'],'--load','--tag','local/central-check:'+a.component.lower(),spec['context']] if spec.get('testTarget') else None
 out=ROOT/'.central-build';metadata=out/(a.component+'.metadata.json')
 build=common+['--target','runtime','--label','org.opencontainers.image.source=https://github.com/'+repo,'--label','org.opencontainers.image.revision='+sha,'--tag',tag,'--metadata-file',str(metadata),'--push' if a.publish else '--load',spec['context']]
 if a.plan:print(json.dumps({'component':a.component,'test':tests,'build':build,'publication':a.publish},indent=2));return
 out.mkdir(exist_ok=True)
 if tests:run(tests)
 run(build)
 meta=json.loads(metadata.read_text());digest=meta.get('containerimage.digest')
 if not re.fullmatch('sha256:[a-f0-9]{64}',digest or ''):raise ValueError('build_digest_missing')
 (out/(a.component+'.json')).write_text(json.dumps({'component':a.component,'sourceRepository':repo,'sourceCommit':sha,'image':tag.split(':sha-')[0]+'@'+digest,'tag':tag,'published':a.publish,'platform':'linux/amd64'},indent=2)+'\n')
if __name__=='__main__':main()
