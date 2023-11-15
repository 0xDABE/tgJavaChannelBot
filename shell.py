import subprocess

out = subprocess.run(input(), shell=True, capture_output=True, text=True)
if out.stderr == '':
    print(out.stdout)
else:
    print(out.stderr)
