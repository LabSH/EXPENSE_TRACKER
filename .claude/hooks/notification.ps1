$stdin = [Console]::OpenStandardInput()
$reader = New-Object System.IO.StreamReader($stdin, [System.Text.Encoding]::UTF8)
$json = $reader.ReadToEnd() | ConvertFrom-Json
[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms') | Out-Null
[System.Windows.Forms.MessageBox]::Show($json.message, 'Claude Code - ' + $json.notification_type)