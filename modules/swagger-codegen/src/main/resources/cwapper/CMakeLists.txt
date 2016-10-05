cmake_minimum_required(VERSION 2.6)
project(petshop)

find_library(LIB_BOOSTER booster)
find_library(LIB_CPPCMS cppcms)

set(CMAKE_CXX_FLAGS -std=c++11)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})

add_executable(api base.cpp service.cpp)
add_dependencies(api ui)

target_link_libraries(api cppcms booster)

add_custom_command(
	OUTPUT ${CMAKE_CURRENT_SOURCE_DIR}/ui
	DEPENDS ui-2.2.5.zip
	COMMAND cd ${CMAKE_CURRENT_SOURCE_DIR} && 7z x -y ui-2.2.5.zip
)

add_custom_target(ui
	DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/ui
)
