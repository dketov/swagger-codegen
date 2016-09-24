#ifndef __CWAPPER_HPP
#define __CWAPPER_HPP

#include <cppcms/application.h>
#include <cppcms/applications_pool.h>
#include <cppcms/service.h>
#include <cppcms/http_response.h>
#include <cppcms/http_request.h>
#include <cppcms/url_mapper.h>
#include <cppcms/url_dispatcher.h>

#include <iostream>
#include <boost/algorithm/string.hpp>

#include <map>
#include <vector>

#include <boost/variant.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/property_tree/exceptions.hpp>
#include <boost/lexical_cast.hpp>

namespace cwapper {
    typedef boost::variant<
        std::string,
        std::vector<std::string>
    > parameter;
    typedef std::map<std::string, parameter> qstring;

    enum contentType { JSON, XML };

    struct data {
        contentType type;
        boost::property_tree::ptree tree;

        data(contentType value = JSON) { type = value; };
        std::string mimeType() const {
            std::string contentTypes[] = { "application/json", "text/xml" };

            return contentTypes[this->type];
        };
    };

    class error: std::exception {};
}

std::ostream& operator<<(std::ostream&, const std::vector<std::string>&);
std::ostream& operator<<(std::ostream&, const cwapper::data&);
std::ostream& operator<<(std::ostream&, const std::vector<cwapper::data>&);
template <typename T>
    cppcms::http::response& operator<<(cppcms::http::response&, const T&);
cppcms::http::response& operator<<(cppcms::http::response&, const cwapper::data&);

#endif
