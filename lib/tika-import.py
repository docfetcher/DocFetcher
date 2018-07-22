#!/usr/bin/python

import sys, os.path as osp, shutil, os

"""
This script copies some of Tika's source files into the DocFetcher source
directory, with some minor processing along the way. Tested with Tika 1.13;
newer Tika versions may require updating this script.

How to use: Run this script with source and destination directory as arguments,
e.g.: /path/to/tika-1.11 /path/to/DocFetcher-1.1/src
"""

# Configuration
core_root = "tika-core/src/main/java/org/apache/tika"
core_packages = """
	concurrent
	config
	detect
	exception
	extractor
	io
	language
	metadata
	mime
	parser
	sax
	utils
"""
parser_root = "tika-parsers/src/main/java/org/apache/tika"
parser_packages = """
	parser/chm
	parser/mp3
	parser/odf
	parser/rtf
	parser/xml
"""
files_to_delete = """
	ChmParser.java
	JackcessExtractor.java
	JackcessParser.java
	package-info.java
	TikaActivator.java
"""
mimetypes_path = "tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml"

# A stripped-down version of org.apache.tika.parser.microsoft.OfficeParser.
# We're using this one instead of the original to avoid dragging half of Tika
# into DocFetcher.
OfficeParser = """
package org.apache.tika.parser.microsoft;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.mime.MediaType;

public class OfficeParser {
    public enum POIFSDocumentType {
        WORKBOOK("xls", MediaType.application("vnd.ms-excel")),
        OLE10_NATIVE("ole", POIFSContainerDetector.OLE10_NATIVE),
        COMP_OBJ("ole", POIFSContainerDetector.COMP_OBJ),
        WORDDOCUMENT("doc", MediaType.application("msword")),
        UNKNOWN("unknown", MediaType.application("x-tika-msoffice")),
        ENCRYPTED("ole", MediaType.application("x-tika-ooxml-protected")),
        POWERPOINT("ppt", MediaType.application("vnd.ms-powerpoint")),
        PUBLISHER("pub", MediaType.application("x-mspublisher")),
        PROJECT("mpp", MediaType.application("vnd.ms-project")),
        VISIO("vsd", MediaType.application("vnd.visio")),
        WORKS("wps", MediaType.application("vnd.ms-works")),
        XLR("xlr", MediaType.application("x-tika-msworks-spreadsheet")),
        OUTLOOK("msg", MediaType.application("vnd.ms-outlook")),
        SOLIDWORKS_PART("sldprt", MediaType.application("sldworks")),
        SOLIDWORKS_ASSEMBLY("sldasm", MediaType.application("sldworks")),
        SOLIDWORKS_DRAWING("slddrw", MediaType.application("sldworks"));

        private final String extension;
        private final MediaType type;

        POIFSDocumentType(String extension, MediaType type) {
            this.extension = extension;
            this.type = type;
        }

        public static POIFSDocumentType detectType(POIFSFileSystem fs) {
            return detectType(fs.getRoot());
        }

        public static POIFSDocumentType detectType(NPOIFSFileSystem fs) {
            return detectType(fs.getRoot());
        }

        public static POIFSDocumentType detectType(DirectoryEntry node) {
            Set<String> names = new HashSet<String>();
            for (Entry entry : node) {
                names.add(entry.getName());
            }
            MediaType type = POIFSContainerDetector.detect(names, node);
            for (POIFSDocumentType poifsType : values()) {
                if (type.equals(poifsType.type)) {
                    return poifsType;
                }
            }
            return UNKNOWN;
        }

        public String getExtension() {
            return extension;
        }

        public MediaType getType() {
            return type;
        }
    }

}
"""

# Get source and destination path
if len(sys.argv) <= 2:
	msg = "Expected source path (e.g. /path/to/tika-1.11) " + \
		"and destination path (e.g. DocFetcher-1.1/src)."
	print(msg)
	exit(0)
src_root = sys.argv[1]
dst_root = sys.argv[2]
for path in [src_root, dst_root]:
	if not osp.isdir(path):
		print("Not a directory: " + path)
		exit(0)
dst_root = osp.join(dst_root, "org/apache/tika")

# Clean destination path
msg = "Contents of '%s' will be deleted. Continue? [y/n] "
msg = msg % dst_root
if raw_input(msg).strip() != "y": exit(0)
if osp.isdir(dst_root):
	shutil.rmtree(dst_root)
os.makedirs(dst_root)

# Copy core and parser packages
d = {core_root: core_packages, parser_root: parser_packages}
for package_root, packages in d.items():
	for package in packages.strip().split("\n"):
		package = package.strip()
		src_dir = osp.join(src_root, package_root, package)
		dst_dir = osp.join(dst_root, package)
		shutil.copytree(src_dir, dst_dir)

# Copy Tika.java
src_path = osp.join(src_root, core_root, "Tika.java")
dst_path = osp.join(dst_root, "Tika.java")
shutil.copyfile(src_path, dst_path)

# Copy mimetypes file
# Needed by RTF parser. Without this file, the parser will crash on RTF files
# containing images. See:
# https://sourceforge.net/p/docfetcher/bugs/1230/
src_path = osp.join(src_root, mimetypes_path)
dst_path = osp.join(dst_root, "mime", "tika-mimetypes.xml")
shutil.copyfile(src_path, dst_path)

# Write OfficeParser -- dependency of Tika's RTF parser
dst_path = osp.join(dst_root, "parser/microsoft")
if not osp.exists(dst_path):
	os.makedirs(dst_path)
dst_path = osp.join(dst_path, "OfficeParser.java")
with open(dst_path, "w") as f:
	f.write(OfficeParser.strip())

# Write POIFSContainerDetector -- dependency of OfficeParser
src_path = osp.join(src_root, parser_root, "parser/microsoft/POIFSContainerDetector.java")
dst_path = osp.join(dst_root, "parser/microsoft/POIFSContainerDetector.java")
with open(src_path, "r") as f_src:
	with open(dst_path, "w") as f_dst:
		f_dst.write(f_src.read())

# Fix TextExtractor: Disable assertions that would otherwise cause DocFetcher to
# crash on some RTF files created by TextMaker. See:
# http://sourceforge.net/p/docfetcher/discussion/702424/thread/8a3dd4f6/
dst_path = osp.join(dst_root, "parser/rtf/TextExtractor.java")
contents = ""
with open(dst_path, "r") as f:
	contents = f.read()
contents = contents.replace("assert param == 0;", "// assert param == 0;")
with open(dst_path, "w") as f:
	f.write(contents)

# Remove files
delete_set = set((x.strip() for x in files_to_delete.strip().split("\n")))
for root, dirs, files in os.walk(dst_root):
	for filename in files:
		if filename in delete_set:
			os.remove(osp.join(root, filename))
