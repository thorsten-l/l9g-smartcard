# Set the path to Java 21
$env:JAVA_HOME = & "$($env:ProgramFiles)\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Run Maven to clean and package the application
mvn clean package

# Prepare installer directory
New-Item -Path "./private/installer" -ItemType Directory -Force | Out-Null
Copy-Item -Path "config.yaml" -Destination "./private/installer" -Force
Copy-Item -Path "target/l9g-webapp-smartcard-monitor.jar" -Destination "./private/installer" -Force

# Change to installer directory
Set-Location -Path "./private/installer"

# Clean up any previous builds
Remove-Item -Path "../smartcard-monitor.exe" -Force -ErrorAction SilentlyContinue

# Use jpackage to create a Windows installer
jpackage `
    --input "." `
    --dest ".." `
    --name "smartcard-monitor" `
    --main-jar "l9g-webapp-smartcard-monitor.jar" `
    --main-class "l9g.webapp.smartcardmonitor.Application" `
    --type "exe" `
    --vendor "l9g" `
    --java-options '--add-modules java.base,java.logging -Xms512m -Xmx1024m' `
    --app-version "1.0.0" `
    --win-shortcut `
    --win-menu `
    --win-dir-chooser `
    --win-per-user-install `
    --win-menu-group "l9g" `
    --win-upgrade-uuid "your-unique-uuid-here" `
    --win-console `
    --description "Smartcard Monitor Windows Service"

# Define variables for WinSW setup
$serviceName = "SmartcardMonitorService"
$serviceDisplayName = "Smartcard Monitor Service"
$serviceDescription = "Smartcard Monitor Windows Service for Smartcard Monitoring"
$winswExecutable = "WinSW.NET5.exe"  # Use the appropriate WinSW executable
$winswDownloadUrl = "https://github.com/winsw/winsw/releases/latest/download/$winswExecutable"
$winswPath = "../$serviceName.exe"

# Download WinSW executable
Invoke-WebRequest -Uri $winswDownloadUrl -OutFile $winswPath -UseBasicParsing

# Create WinSW XML configuration for the service
$winswConfig = @"
<service>
  <id>$serviceName</id>
  <name>$serviceDisplayName</name>
  <description>$serviceDescription</description>
  <executable>%BASE%\smartcard-monitor.exe</executable>
  <arguments>-jar %BASE%\l9g-webapp-smartcard-monitor.jar</arguments>
  <logpath>%BASE%\logs</logpath>
  <log mode="roll"/>
</service>
"@
$winswConfig | Out-File -Encoding utf8 -FilePath "../$serviceName.xml"

# Install the service using WinSW
& "$winswPath" install

# Start the service
Start-Service -Name $serviceName

Write-Output "Service $serviceDisplayName installed and started successfully."
