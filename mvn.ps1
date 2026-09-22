$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $mavenCommand) {
    Write-Error "Maven is not installed or is not on PATH. Install Maven 3.9+ first."
    exit 1
}
& $mavenCommand.Source @args
exit $LASTEXITCODE
