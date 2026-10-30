{{- define "tracker.labels" -}}
app.kubernetes.io/part-of: job-application-tracker
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
