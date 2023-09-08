#!/bin/bash

stylesheet="${1}"
shift

for file in "$@"; do
	echo "Processing '${file}'"
	xsltproc --encoding "ISO-8859-1" "${stylesheet}" "${file}" > "${file}.tmp" || exit 1
	unix2dos "${file}.tmp" || exit 1
	mv "${file}.tmp" "${file}"
done
