.PHONY: all demo

GROUP := info.voidstar.tool.cli
ARTIFACT := mccli
VERSION := 0.1.0-SNAPSHOT

# https://stackoverflow.com/a/5982798
THIS_MAKEFILE_PATH:=$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
THIS_DIR:=$(shell cd $(dir $(THIS_MAKEFILE_PATH));pwd)
THIS_MAKEFILE:=$(notdir $(THIS_MAKEFILE_PATH))

export PATH:=$(THIS_DIR):$(PATH)

all: $(ARTIFACT)

$(ARTIFACT): coursier $(THIS_DIR)/build.sbt $(shell find "$(THIS_DIR)/src" -type f \( -name '*.scala' -o -name '*.java' \))
	sbt publishLocal
	coursier bootstrap -o $@ -f --standalone -M $@.Main "$(GROUP)::$(ARTIFACT):$(VERSION)"

coursier:
	src="https://git.io/coursier-cli"; \
        { 2>/dev/null hash curl && curl -Lo- "$$src" > "$@"; } || \
        { 2>/dev/null hash wget && wget -qO- "$$src" > "$@"; }
	chmod +x "$@"

demo: $(ARTIFACT)
	$< -h
	$< -g com.lihaoyi -a ammonite_2.1* -r 50
